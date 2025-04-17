import java.util.*;

/**
 * run using thoose commaneds lines
 * javac FrmsCrewModule.java
 * java -ea FrmsCrewModule

 * for: Registration, Assign Manager, Add/Remove Crew.
 */
public class FrmsCrewModule {

    // ------------------ DATA STRUCTURES ------------------

    /** User roles */
    enum Role {
        CUSTOMER, CREW, MANAGER, OWNER
    }

    /** Basic user record */
    static class User {
        final String id; // unique
        final String name;
        Role role;

        User(String id, String name, Role role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }
    }

    /** In‑memory database : userID -> User */
    private final Map<String, User> users = new HashMap<>();

    // ------------------ INVARIANT ------------------
    /**
     * * Every user id in `users` is unique (HashMap guarantees)
     * * The number of MANAGERs is ≤ total number of CREW+MANAGER
     */
    private void checkInvariant() {
        long mgr = users.values().stream().filter(u -> u.role == Role.MANAGER).count();
        long crewPlusMgr = users.values().stream()
                .filter(u -> u.role == Role.CREW || u.role == Role.MANAGER)
                .count();
        assert mgr <= crewPlusMgr : "Invariant violated: more MANAGERs than (CREW + MANAGER)!";
    }

    // ------------------ USE‑CASE 1: REGISTRATION ------------------
    /**
     * Preconditions
     * • id & name are non‑null, non‑empty
     * • id is not already taken
     * Postconditions
     * • users map contains a new entry with that id
     */
    public void registerUser(String id, String name, Role role) {
        // pre
        assert id != null && !id.isBlank() : "id cannot be null/blank";
        assert name != null && !name.isBlank() : "name cannot be null/blank";
        assert !users.containsKey(id) : "User id already exists";

        // process
        users.put(id, new User(id, name, role == null ? Role.CUSTOMER : role));

        // invariant
        checkInvariant();

        // post
        assert users.containsKey(id) : "Registration failed";
        System.out.println("Registered user '" + id + "' as " + users.get(id).role);
    }

    // ------------------ USE‑CASE 2: ASSIGN MANAGER ------------------
    /**
     * Promotes an existing staff to MANAGER.
     *
     * Preconditions
     * • id exists
     * • current role is CREW (can’t promote customer/owner here)
     * Postconditions
     * • user's role == MANAGER
     */
    public void assignManager(String id) {
        // pre
        assert users.containsKey(id) : "No such user";
        assert users.get(id).role == Role.CREW : "Only CREW can be promoted";

        // process
        users.get(id).role = Role.MANAGER;

        // invariant
        checkInvariant();

        // post
        assert users.get(id).role == Role.MANAGER : "Promotion failed";
        System.out.println("User '" + id + "' is now MANAGER");
    }

    // ------------------ USE‑CASE 3a: ADD CREW ------------------
    /**
     * Adds a new crew member.
     * Preconditions & postconditions like registration, but role fixed to CREW.
     */
    public void addCrew(String id, String name) {
        registerUser(id, name, Role.CREW); // re‑use logic
    }

    // ------------------ USE‑CASE 3b: REMOVE CREW ------------------
    /**
     * Safely removes a crew/manager from roster.
     *
     * Preconditions
     * • id exists
     * • role is CREW or MANAGER
     * Postconditions
     * • id no longer in users map
     */
    public void removeCrew(String id) {
        // pre
        assert users.containsKey(id) : "No such staff";
        assert users.get(id).role == Role.CREW ||
                users.get(id).role == Role.MANAGER : "Can remove only staff";

        // process
        users.remove(id);

        // invariant
        checkInvariant();

        // post
        assert !users.containsKey(id) : "Removal failed";
        System.out.println("Staff '" + id + "' removed");
    }

    // ------------------ DEMO MAIN ------------------
    public static void main(String[] args) {
        FrmsCrewModule sys = new FrmsCrewModule();

        // 1. Registration examples
        sys.registerUser("cust01", "Alice", null); // default CUSTOMER
        sys.registerUser("owner1", "Bob", Role.OWNER);

        // 2. Add crew, promote, remove
        sys.addCrew("crew01", "Charlie");
        sys.assignManager("crew01");
        sys.removeCrew("crew01");

        // contract violation (duplicate id)
        sys.registerUser("cust01", "Duplicate", null);
    }
}
