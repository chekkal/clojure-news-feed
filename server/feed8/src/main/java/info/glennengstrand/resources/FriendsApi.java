/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.0-SNAPSHOT).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package info.glennengstrand.resources;

import java.util.List;

import info.glennengstrand.api.Friend;

public interface FriendsApi {
      Friend addFriend(Friend body);
      List<Friend> getFriend(Long id);

}
