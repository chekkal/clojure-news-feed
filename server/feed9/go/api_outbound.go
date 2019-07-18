package newsfeedserver

import (
        "os"
        "fmt"
	"log"
	"time"
	"sync"
	"reflect"
	"strconv"
	"net/http"
	"encoding/json"
	"github.com/google/uuid"
	"github.com/gorilla/mux"
	"github.com/gocql/gocql"
	"gopkg.in/olivere/elastic.v3"
)

var esPool = &sync.Pool{
	New: func() interface{} {
		eshost := fmt.Sprintf("http://%s:9200", os.Getenv("SEARCH_HOST"))
		esclient, err := elastic.NewClient(elastic.SetURL(eshost))
		if err != nil {
	   	   log.Printf("cannot connect to elasticsearch: %s", err)
		}
		return esclient
	},
}

type OutboundStoryDocument struct {
     	Id string `json:"id"`
	Sender string `json:"sender"`
	Story string `json:"story"`
}

var ElasticSearchIndexer = make(chan OutboundStoryDocument)

func handleIndexRequest() {
	eshost := fmt.Sprintf("http://%s:9200", os.Getenv("SEARCH_HOST"))
	esclient, err := elastic.NewClient(elastic.SetURL(eshost))
	if err != nil {
	   log.Printf("cannot connect to elasticsearch: %s", err)
	}
	for {
     	    req := <- ElasticSearchIndexer
	    esclient.Index().
		Index("feed").
		Type("stories").
		Id(req.Id).
		BodyJson(req).
		Do()
        }
}

func init() {
        go handleIndexRequest()
        go handleIndexRequest()
}

func AddOutbound(w http.ResponseWriter, r *http.Request) {
   	decoder := json.NewDecoder(r.Body)
    	var ob Outbound
    	err := decoder.Decode(&ob)
	if err != nil {
	    msg := fmt.Sprintf("outbound body error: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusBadRequest)
	    return
	}
	cluster := gocql.NewCluster(os.Getenv("NOSQL_HOST"))
	cluster.Keyspace = os.Getenv("NOSQL_KEYSPACE")
	cluster.Timeout = 10 * time.Second
	cluster.ConnectTimeout = 20 * time.Second
	cluster.Consistency = gocql.Any
	session, err := cluster.CreateSession()
	if err != nil {
	    msg := fmt.Sprintf("cannot create cassandra session: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusInternalServerError)
	    return
	}
	defer session.Close()
	id := strconv.FormatInt(ob.From, 10)
	_, friends, err := GetFriendsInner(id)
	if err != nil {
	   msg := fmt.Sprintf("system error while fetching friends for %s", id)
	   log.Println(msg)
	   http.Error(w, msg, http.StatusInternalServerError)
	   return
	}
	esidr, err := uuid.NewRandom()
	if err != nil {
	   msg := fmt.Sprintf("cannot generate a random id: %s", err)
	   log.Println(msg)
	   http.Error(w, msg, http.StatusInternalServerError)
	   return
	}
	esid := fmt.Sprintf("%s", esidr)
	for _, friend := range friends {
	   inb := Inbound {
	      From: ob.From,
	      To: friend.To,
	      Occurred: ob.Occurred,
	      Subject: ob.Subject,
	      Story: ob.Story,
	   }
	   AddInbound(inb, session)
	}
	stmt := session.Query("insert into Outbound (ParticipantID, Occurred, Subject, Story) values (?, now(), ?, ?) using ttl 7776000", ob.From, ob.Subject, ob.Story)
	stmt.Consistency(gocql.One)
	stmt.Exec()
	osd := OutboundStoryDocument{
	    Id: esid,
	    Sender: id,
	    Story: ob.Story,
	}
	ElasticSearchIndexer <- osd
	resultb, err := json.Marshal(ob)
	if err != nil {
	    msg := fmt.Sprintf("cannot marshal outbound response: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusInternalServerError)
	    return
	}
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	fmt.Fprint(w, string(resultb))
	w.WriteHeader(http.StatusOK)
}

func GetOutbound(w http.ResponseWriter, r *http.Request) {
	cluster := gocql.NewCluster(os.Getenv("NOSQL_HOST"))
	cluster.Keyspace = os.Getenv("NOSQL_KEYSPACE")
	cluster.Timeout = 10 * time.Second
	cluster.ConnectTimeout = 20 * time.Second
	cluster.Consistency = gocql.One
	session, err := cluster.CreateSession()
	if err != nil {
	    msg := fmt.Sprintf("cannot create cassandra session: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusInternalServerError)
	    return
	}
	defer session.Close()

	vars := mux.Vars(r)
	from, err := strconv.ParseInt(vars["id"], 0, 64)
	if err != nil {
	    msg := fmt.Sprintf("id is not an integer: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusBadRequest)
	    return
	}
	stmt := session.Query("select toTimestamp(occurred) as occurred, subject, story from Outbound where participantid = ? order by occurred desc", vars["id"])
	stmt.Consistency(gocql.One)
	iter := stmt.Iter()
	defer iter.Close()
	var occurred time.Time
	var subject string
	var story string
	var results []Outbound
	for iter.Scan(&occurred, &subject, &story) {
	    ob := Outbound {
	      From: from,
	      Occurred: occurred,
	      Subject: subject,
	      Story: story,
	    }
	    results = append(results, ob)
	}
	resultb, err := json.Marshal(results)
	if err != nil {
	    msg := fmt.Sprintf("cannot marshal data: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusInternalServerError)
	    return
	}
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	fmt.Fprint(w, string(resultb))
	w.WriteHeader(http.StatusOK)
}

func SearchOutbound(w http.ResponseWriter, r *http.Request) {
	keywords, ok := r.URL.Query()["keywords"]
	if !ok || len(keywords[0]) < 1 {
	   msg := fmt.Sprint("must specify keywords")
	   log.Println(msg)
	   http.Error(w, msg, http.StatusBadRequest)
	   return
	}
	esclient := esPool.Get().(*elastic.Client)
	query := elastic.NewMatchQuery("story", string(keywords[0]))
	searchResult, err := esclient.Search().
		      Index("feed").
		      Query(query).
		      Do()
	if err != nil {
	   msg := fmt.Sprintf("cannot query elasticsearch: %s", err)
	   log.Println(msg)
	   http.Error(w, msg, http.StatusInternalServerError)
	   return
	}
	esPool.Put(esclient)
	var osd OutboundStoryDocument
	var results []string
	for _, result := range searchResult.Each(reflect.TypeOf(osd)) {
	    doc, ok := result.(OutboundStoryDocument)
	    if ok {
	       results = append(results, doc.Sender)
	    }
	}
	resultb, err := json.Marshal(results)
	if err != nil {
	    msg := fmt.Sprintf("cannot marshal outbound results: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusInternalServerError)
	    return
	}
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	fmt.Fprint(w, string(resultb))
	w.WriteHeader(http.StatusOK)
}