package app.elizon.perms.pkg.group.trace;

public interface GroupTrace {

    String oldGroup();

    String newGroup();

    boolean temporary();

    long changeDateTimeStamp();

}
