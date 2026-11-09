package canteen.model;

/** Holds the currently logged-in user (very small "session" for a desktop app). */
public class Session {
    public static int userId;
    public static String role;      // OWNER or CUSTOMER
    public static int studentId;    // 0 for the owner
    public static String fullName;
    public static String username;

    public static void clear() { userId = 0; role = null; studentId = 0; fullName = null; username = null; }
    public static boolean isOwner() { return "OWNER".equals(role); }
}
