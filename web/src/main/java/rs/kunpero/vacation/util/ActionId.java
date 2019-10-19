package rs.kunpero.vacation.util;

public enum ActionId {
    ADD_VACATION, SHOW_VACATION, DELETE_VACATION, CLOSE_DIALOG,
    SET_FROM, SET_SUBSTITUTION, SET_TO, OTHER;

    public static ActionId safeValueOf(String value) {
        try {
            return ActionId.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
