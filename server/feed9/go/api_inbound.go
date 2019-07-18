package newsfeedserver

import (
        "os"
        "fmt"
	"log"
	"time"
	"strconv"
	"net/http"
	"encoding/json"
	"github.com/gorilla/mux"
	"github.com/gocql/gocql"
)

func AddInbound(i Inbound, session *gocql.Session) {
	stmt := session.Query("insert into Inbound (ParticipantID, FromParticipantID, Occurred, Subject, Story) values (?, ?, now(), ?, ?) using ttl 7776000", i.To, i.From, i.Subject, i.Story)
	stmt.Consistency(gocql.Any)
	stmt.Exec()
}

func GetInbound(w http.ResponseWriter, r *http.Request) {
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
	i, err := strconv.ParseInt(vars["id"], 0, 64)
	if err != nil {
	    msg := fmt.Sprintf("id is not an integer: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusInternalServerError)
	    return
	}
	stmt := session.Query("select toTimestamp(occurred) as occurred, fromparticipantid, subject, story from Inbound where participantid = ? order by occurred desc", vars["id"])
	stmt.Consistency(gocql.One)
	iter := stmt.Iter()
	defer iter.Close()
	var occurred time.Time
	var from int64
	var subject string
	var story string
	var results []Inbound
	for iter.Scan(&occurred, &from, &subject, &story) {
	    inb := Inbound {
	      From: from,
	      To: i,
	      Occurred: occurred,
	      Subject: subject,
	      Story: story,
	    }
	    results = append(results, inb)
	}
	resultb, err := json.Marshal(results)
	if err != nil {
	    msg := fmt.Sprintf("cannot marshal inbound result: %s", err)
	    log.Println(msg)
	    http.Error(w, msg, http.StatusInternalServerError)
	    return
	}
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	fmt.Fprint(w, string(resultb))
	w.WriteHeader(http.StatusOK)
}