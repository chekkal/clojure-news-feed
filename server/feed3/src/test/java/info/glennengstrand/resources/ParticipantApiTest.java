/**
 * News Feed
 * news feed api
 *
 * OpenAPI spec version: 1.0.0
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Eclipse Public License - v 1.0
 *
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package info.glennengstrand.resources;

import info.glennengstrand.NewsFeedConfiguration;
import info.glennengstrand.NewsFeedModule.ParticipantCache;
import info.glennengstrand.api.Participant;
import info.glennengstrand.core.MessageLogger;
import info.glennengstrand.core.ParticipantApiServiceImpl;
import info.glennengstrand.db.ParticipantDAO;
import info.glennengstrand.resources.ParticipantApi.ParticipantApiService;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * API tests for ParticipantApi
 */
public class ParticipantApiTest extends NewsFeedTestBase {

	private static final String TEST_MONIKER = "test";
	private static final long TEST_ID = 1l;
	
    private ParticipantApiService api = null;
    private Participant participant = null;
    private ParticipantCache cache = null;
    private ParticipantDAO dao = null;

    @Before
    public void setup() {
    	participant = new Participant.ParticipantBuilder()
    			.withId(TEST_ID)
    			.withName(TEST_MONIKER)
    			.build();
    	cache = new PassThroughParticipantCache(Participant.class, null);
    	dao = mock(ParticipantDAO.class);
    	when(dao.upsertParticipant(any(String.class))).thenReturn(TEST_ID);
    	when(dao.fetchParticipant(TEST_ID)).thenReturn(participant);
    	api = new  ParticipantApiServiceImpl(dao, cache, new MessageLogger.DoNothingMessageLogger());
    }
    
    /**
     * create a new participant
     *
     * a participant is someone who can post news to friends
     *
     */
    @Test
    public void addParticipantTest() {
        Participant response = api.addParticipant(participant);
        assertTrue(String.format("Expected ID to be %d but instead it was %d", TEST_ID, response.getId()), TEST_ID == response.getId()); 
    }
    
    /**
     * retrieve an individual participant
     *
     * fetch a participant by id
     *
     */
    @Test
    public void getParticipantTest() {
        Participant response = api.getParticipant(TEST_ID);
        assertTrue(String.format("expected participant name to be %s but instead it was %s", TEST_MONIKER, response.getName()), response.getName().equals(TEST_MONIKER));
    }
    
    private class PassThroughParticipantCache extends ParticipantCache {

		public PassThroughParticipantCache(Class<Participant> serializationType, NewsFeedConfiguration config) {
			super(serializationType, config);
		}

		@Override
		public Participant get(Long id, Supplier<Participant> loader) {
			return loader.get();
		}

		@Override
		public List<Participant> getMulti(Long id, Supplier<List<Participant>> loader) {
			return loader.get();
		}

		@Override
		public void invalidate(Long id) {
		}
    	
    }
    
}
