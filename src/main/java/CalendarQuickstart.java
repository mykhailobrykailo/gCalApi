import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.client.util.DateTime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

// BASIC SETUP QUICKSTART , main is CalCOntroller
public class CalendarQuickstart {
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES =
      Collections.singletonList(CalendarScopes.CALENDAR);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        
    // Build flow and trigger user authorization request. (SAVES AUTHORIZATION TOKENS< DONT HAVE TO RESIGN IN)
    // GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
    //     HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
    //     .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
    //     .setAccessType("offline")
    //     .build();


    //same as above but REMOVES SAVING AUTH TOKENS SO YOU SIGN IN EVERY TIME
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setAccessType("offline")
      .build();


    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }

  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Calendar service =
        new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();



    // List the next 10 events from the primary calendar.
    DateTime now = new DateTime(System.currentTimeMillis());
    Events events = service.events().list("primary")
        .setMaxResults(10)
        .setTimeMin(now)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute();
    List<Event> items = events.getItems();
    if (items.isEmpty()) {
      System.out.println("No upcoming events found.");
    } else {
      System.out.println("Upcoming events");
      for (Event event : items) {
        DateTime start = event.getStart().getDateTime();
        if (start == null) {
          start = event.getStart().getDate();
        }
        System.out.printf("%s (%s)\n", event.getSummary(), start);
      }


      // Refer to the Java quickstart on how to setup the environment:
// https://developers.google.com/calendar/quickstart/java
// Change the scope to CalendarScopes.CALENDAR and delete any stored
// credentials.

      Event testEvent = new Event()
              .setSummary("Google I/O 2015")
              .setLocation("800 Howard St., San Francisco, CA 94103")
              .setDescription("A chance to hear more about Google's developer products.");

      DateTime startDateTime = new DateTime("2015-05-28T09:00:00-07:00");
      EventDateTime start = new EventDateTime()
              .setDateTime(startDateTime)
              .setTimeZone("America/Los_Angeles");
      testEvent.setStart(start);

      DateTime endDateTime = new DateTime("2015-05-28T17:00:00-07:00");
      EventDateTime end = new EventDateTime()
              .setDateTime(endDateTime)
              .setTimeZone("America/Los_Angeles");
      testEvent.setEnd(end);

      String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
      testEvent.setRecurrence(Arrays.asList(recurrence));

      EventAttendee[] attendees = new EventAttendee[] {
              new EventAttendee().setEmail("lpage@example.com"),
              new EventAttendee().setEmail("sbrin@example.com"),
      };
      testEvent.setAttendees(Arrays.asList(attendees));

      EventReminder[] reminderOverrides = new EventReminder[] {
              new EventReminder().setMethod("email").setMinutes(24 * 60),
              new EventReminder().setMethod("popup").setMinutes(10),
      };
      Event.Reminders reminders = new Event.Reminders()
              .setUseDefault(false)
              .setOverrides(Arrays.asList(reminderOverrides));
      testEvent.setReminders(reminders);

      String calendarId = "primary";
      testEvent = service.events().insert(calendarId, testEvent).execute();
      System.out.printf("Event created: %s\n", testEvent.getHtmlLink());



    }
  }
}