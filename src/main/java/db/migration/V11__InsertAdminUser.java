package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;
import java.util.Arrays;

public class V11__InsertAdminUser implements JdbcMigration {
    private static final String INS_USER =
            "INSERT INTO users(account, name, email, write_protected)"
                    + "VALUES(?,?,?,?)";

    /* H2 Only */
    private static final String INS_PASSWD_CRED =
            "INSERT INTO password_credentials(user_id, password, salt)"
                    + "VALUES(?,HASH('SHA256', STRINGTOUTF8(CONCAT('0123456789012345', ?)), 100),?)";

    private static final String INS_GROUP =
            "INSERT INTO groups(name, description, write_protected) VALUES(?,?,?)";

    private static final String INS_ROLE =
            "INSERT INTO roles(name, description, write_protected) VALUES(?,?,?)";

    private static final String INS_PERMISSION =
            "INSERT INTO permissions(name, description, write_protected) VALUES(?,?,?)";

    private static final String INS_ROLE_PERMISSION =
            "INSERT INTO role_permissions(role_id, permission_id) VALUES(?, ?)";

    private static final String INS_MEMBERSHIP =
            "INSERT INTO memberships(user_id, group_id) VALUES(?, ?)";

    private static final String INS_APPLICATION =
            "INSERT INTO applications(name, description, pass_to, virtual_path, top_page, write_protected) VALUES(?,?,?,?,?,?)";

    private static final String INS_REALM =
            "INSERT INTO realms(name, url, application_id, description, write_protected) VALUES(?, ?, ?, ?,?)";

    private static final String INS_ASSIGNMENT =
            "INSERT INTO assignments(group_id, role_id, realm_id) VALUES(?, ?, ?)";

    private static final String[] ADMIN_PERMISSIONS = new String[]{
            "LIST_ANY_USERS", "CREATE_USER", "MODIFY_ANY_USER", "DELETE_ANY_USER",
            "LOCK_ANY_USER", "UNLOCK_ANY_USER",
            "LIST_ANY_GROUPS", "CREATE_GROUP", "MODIFY_ANY_GROUP", "DELETE_ANY_GROUP",
            "CREATE_MEMBERSHIP", "DELETE_MEMBERSHIP",
            "LIST_ANY_APPLICATIONS", "CREATE_APPLICATION", "MODIFY_ANY_APPLICATION", "DELETE_ANY_APPLICATION",
            "LIST_ANY_REALMS", "CREATE_REALM", "MODIFY_ANY_REALM", "DELETE_ANY_REALM",
            "LIST_ANY_ROLES", "CREATE_ROLE", "MODIFY_ANY_ROLE", "DELETE_ANY_ROLE",
            "LIST_ANY_PERMISSIONS", "CREATE_PERMISSION", "MODIFY_ANY_PERMISSION", "DELETE_ANY_PERMISSION",
            "LIST_OAUTH2_APPLICATIONS", "MODIFY_OAUTH2_APPLICATION", "DELETE_OAUTH2_APPLICATION",
            "LIST_OAUTH2_PROVIDERS", "MODIFY_OAUTH2_PROVIDER", "DELETE_OAUTH2_PROVIDER",
            "CREATE INVITATION"
    };

    private static final String[] OTHER_PERMISSIONS = new String[]{
            "LIST_USERS", "MODIFY_USER", "DELETE_USER",
            "LOCK_USER", "UNLOCK_USER",
            "LIST_GROUPS", "MODIFY_GROUP", "DELETE_GROUP",
            "LIST_APPLICATIONS", "MODIFY_APPLICATION", "DELETE_APPLICATION",
            "LIST_REALMS", "MODIFY_REALM", "DELETE_REALM",
            "LIST_ROLES", "MODIFY_ROLE", "DELETE_ROLE",
            "LIST_PERMISSIONS", "MODIFY_PERMISSION", "DELETE_PERMISSION",
    };
    private Long fetchGeneratedKey(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            while(rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Generated key is not found.");
        }
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        try (PreparedStatement stmtInsUser = connection.prepareStatement(INS_USER, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsPasswdCred = connection.prepareStatement(INS_PASSWD_CRED);
             PreparedStatement stmtInsPermission = connection.prepareStatement(INS_PERMISSION, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsRole = connection.prepareStatement(INS_ROLE, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsRolePermission = connection.prepareStatement(INS_ROLE_PERMISSION);
             PreparedStatement stmtInsMembership = connection.prepareStatement(INS_MEMBERSHIP, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsGroup = connection.prepareStatement(INS_GROUP, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsRealm = connection.prepareStatement(INS_REALM, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsApplication = connection.prepareStatement(INS_APPLICATION, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsAssignment = connection.prepareStatement(INS_ASSIGNMENT)
             ) {
            stmtInsUser.setString(1, "admin");
            stmtInsUser.setString(2, "Admin User");
            stmtInsUser.setString(3, "admin@example.com");
            stmtInsUser.setBoolean(4, true);
            stmtInsUser.executeUpdate();
            Long userId = fetchGeneratedKey(stmtInsUser);

            stmtInsPasswdCred.setLong(1, userId);
            stmtInsPasswdCred.setString(2, "password");
            stmtInsPasswdCred.setString(3, "0123456789012345");
            stmtInsPasswdCred.executeUpdate();

            stmtInsGroup.setString(1, "BOUNCR_ADMIN");
            stmtInsGroup.setString(2, "Bouncr administrators");
            stmtInsGroup.setBoolean(3, true);
            stmtInsGroup.executeUpdate();
            Long adminGroupId = fetchGeneratedKey(stmtInsGroup);

            stmtInsGroup.setString(1, "BOUNCR_USER");
            stmtInsGroup.setString(2, "Bouncr users");
            stmtInsGroup.setBoolean(3, true);
            stmtInsGroup.executeUpdate();
            Long userGroupId = fetchGeneratedKey(stmtInsGroup);

            stmtInsMembership.setLong(1, userId);
            stmtInsMembership.setLong(2, adminGroupId);
            stmtInsMembership.executeUpdate();

            stmtInsMembership.setLong(1, userId);
            stmtInsMembership.setLong(2, userGroupId);
            stmtInsMembership.executeUpdate();

            stmtInsApplication.setString(1, "BOUNCR");
            stmtInsApplication.setString(2, "Bouncer application");
            stmtInsApplication.setString(3, "");
            stmtInsApplication.setString(4, "/");
            stmtInsApplication.setString(5, "/my");
            stmtInsApplication.setBoolean(6, true);
            stmtInsApplication.executeUpdate();
            Long applicationId = fetchGeneratedKey(stmtInsApplication);

            stmtInsRealm.setString(1, "BOUNCR");
            stmtInsRealm.setString(2, "^/(admin|my)($|/.*)");
            stmtInsRealm.setLong(3, applicationId);
            stmtInsRealm.setString(4, "Bouncr Application Realm");
            stmtInsRealm.setBoolean(5, true);
            stmtInsRealm.executeUpdate();
            Long bouncrRealmId = fetchGeneratedKey(stmtInsRealm);

            stmtInsRole.setString(1, "BOUNCR_ADMIN");
            stmtInsRole.setString(2, "Bouncer administrations");
            stmtInsRole.setBoolean(3, true);
            stmtInsRole.executeUpdate();
            Long adminRoleId = fetchGeneratedKey(stmtInsRole);

            Arrays.asList(ADMIN_PERMISSIONS).forEach(perm -> {
                try {
                    stmtInsPermission.setString(1, perm);
                    stmtInsPermission.setString(2, "");
                    stmtInsPermission.setBoolean(3, true);
                    stmtInsPermission.executeUpdate();
                    Long permissionId = fetchGeneratedKey(stmtInsPermission);

                    stmtInsRolePermission.setLong(1, adminRoleId);
                    stmtInsRolePermission.setLong(2, permissionId);
                    stmtInsRolePermission.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            Arrays.asList(OTHER_PERMISSIONS).forEach(perm -> {
                try {
                    stmtInsPermission.setString(1, perm);
                    stmtInsPermission.setString(2, "");
                    stmtInsPermission.setBoolean(3, true);
                    stmtInsPermission.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            stmtInsRole.setString(1, "BOUNCR_USER");
            stmtInsRole.setString(2, "Bouncr users");
            stmtInsRole.setBoolean(3, true);
            stmtInsRole.executeUpdate();
            Long myRoleId = fetchGeneratedKey(stmtInsRole);
            stmtInsPermission.setString(1, "READ_USER");
            stmtInsPermission.setString(2, "");
            stmtInsPermission.setBoolean(3, true);
            stmtInsPermission.executeUpdate();
            Long readUserPermissionId = fetchGeneratedKey(stmtInsPermission);
            stmtInsRolePermission.setLong(1, myRoleId);
            stmtInsRolePermission.setLong(2, readUserPermissionId);
            stmtInsRolePermission.executeUpdate();

            stmtInsAssignment.setLong(1, adminGroupId);
            stmtInsAssignment.setLong(2, adminRoleId);
            stmtInsAssignment.setLong(3, bouncrRealmId);
            stmtInsAssignment.executeUpdate();

            stmtInsAssignment.setLong(1, userGroupId);
            stmtInsAssignment.setLong(2, myRoleId);
            stmtInsAssignment.setLong(3, bouncrRealmId);
            stmtInsAssignment.executeUpdate();
        }
    }
}
