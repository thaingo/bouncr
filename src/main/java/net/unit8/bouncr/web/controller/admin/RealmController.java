package net.unit8.bouncr.web.controller.admin;

import enkan.collection.Parameters;
import enkan.component.BeansConverter;
import enkan.component.doma2.DomaProvider;
import enkan.data.HttpResponse;
import enkan.security.UserPrincipal;
import kotowari.component.TemplateEngine;
import kotowari.routing.UrlRewriter;
import net.unit8.bouncr.component.RealmCache;
import net.unit8.bouncr.web.dao.AssignmentDao;
import net.unit8.bouncr.web.dao.GroupDao;
import net.unit8.bouncr.web.dao.RealmDao;
import net.unit8.bouncr.web.dao.RoleDao;
import net.unit8.bouncr.web.entity.Assignment;
import net.unit8.bouncr.web.entity.Group;
import net.unit8.bouncr.web.entity.Realm;
import net.unit8.bouncr.web.entity.Role;
import net.unit8.bouncr.web.form.RealmForm;
import org.seasar.doma.jdbc.SelectOptions;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static enkan.util.HttpResponseUtils.RedirectStatusCode.SEE_OTHER;

public class RealmController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider daoProvider;

    @Inject
    private BeansConverter converter;

    @Inject
    private RealmCache realmCache;

    @RolesAllowed({"LIST_REALMS", "LIST_ANY_REALMS"})
    public HttpResponse listByApplicationId(Parameters params, UserPrincipal principal) {
        Long applicationId = params.getLong("applicationId");
        RealmDao realmDao = daoProvider.getDao(RealmDao.class);
        SelectOptions options = SelectOptions.get();
        List<Realm> realms = realmDao.selectByApplicationId(applicationId, principal, options);
        return templateEngine.render("admin/realm/list",
                "applicationId", applicationId,
                "realms", realms);
    }

    @RolesAllowed("CREATE_REALM")
    public HttpResponse newForm(RealmForm form) {
        GroupDao groupDao = daoProvider.getDao(GroupDao.class);
        List<Group> groups = groupDao.selectAll();

        RoleDao roleDao = daoProvider.getDao(RoleDao.class);
        List<Role> roles = roleDao.selectAll();
        form.setAssignments(Collections.emptyList());


        return templateEngine.render("admin/realm/new",
                "realm", form,
                "writeProtected", false,
                "groups", groups,
                "roles", roles);
    }

    @Transactional
    @RolesAllowed("CREATE_REALM")
    public HttpResponse create(RealmForm form) {
        if (form.hasErrors()) {
            return templateEngine.render("admin/realm/new",
                    "realm", form);
        } else {
            Realm realm = converter.createFrom(form, Realm.class);
            realm.setWriteProtected(false);
            RealmDao realmDao = daoProvider.getDao(RealmDao.class);
            realmDao.insert(realm);

            createAssign(form, realm);

            return UrlRewriter.redirect(RealmController.class,
                    "listByApplicationId?applicationId=" + form.getApplicationId(), SEE_OTHER);
        }
    }

    @RolesAllowed({"MODIFY_REALM", "MODIFY_ANY_REALM"})
    public HttpResponse edit(Parameters params) {
        RealmDao realmDao = daoProvider.getDao(RealmDao.class);
        Realm realm = realmDao.selectById(params.getLong("id"));

        GroupDao groupDao = daoProvider.getDao(GroupDao.class);
        List<Group> groups = groupDao.selectAll();

        RoleDao roleDao = daoProvider.getDao(RoleDao.class);
        List<Role> roles = roleDao.selectAll();

        AssignmentDao assignmentDao = daoProvider.getDao(AssignmentDao.class);
        List<Assignment> assignments = assignmentDao.selectByRealmId(realm.getId());

        RealmForm form = converter.createFrom(realm, RealmForm.class);
        form.setAssignments(assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getGroupId))
                .entrySet()
                .stream()
                .map(e -> new RealmForm.AssignmentForm(e.getKey(),
                        e.getValue()
                                .stream()
                                .map(Assignment::getRoleId)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList()));

        return templateEngine.render("admin/realm/edit",
                "realm", form,
                "writeProtected", realm.getWriteProtected(),
                "groups", groups,
                "roles", roles);
    }

    @Transactional
    @RolesAllowed({"MODIFY_REALM", "MODIFY_ANY_REALM"})
    public HttpResponse update(RealmForm form) {
        if (form.hasErrors()) {
            return templateEngine.render("admin/realm/new",
                    "realm", form);
        } else {
            Realm realm = converter.createFrom(form, Realm.class);
            realm.setWriteProtected(false);
            RealmDao realmDao = daoProvider.getDao(RealmDao.class);
            realmDao.update(realm);

            createAssign(form, realm);

            return UrlRewriter.redirect(RealmController.class,
                    "listByApplicationId?applicationId=" + form.getApplicationId(), SEE_OTHER);
        }
    }

    private void createAssign(RealmForm form, Realm realm) {
        AssignmentDao assignmentDao = daoProvider.getDao(AssignmentDao.class);
        assignmentDao.selectByRealmId(realm.getId()).forEach(assignmentDao::delete);
        form.getAssignments()
                .stream()
                .filter(a -> a.getGroupId() != null && a.getRoleId() != null)
                .forEach(a -> a.getRoleId().forEach(
                        roleId -> {
                            Assignment assignment = new Assignment();
                            assignment.setGroupId(a.getGroupId());
                            assignment.setRealmId(realm.getId());
                            assignment.setRoleId(roleId);
                            assignmentDao.insert(assignment);
                        }
                ));
        realmCache.refresh();
    }

    @Transactional
    @RolesAllowed({"DELETE_REALM", "DELETE_ANY_REALM"})
    public HttpResponse delete(Parameters params) {
        RealmDao realmDao = daoProvider.getDao(RealmDao.class);
        Realm realm = realmDao.selectById(params.getLong("id"));
        realmDao.delete(realm);
        return UrlRewriter.redirect(RealmController.class,
                "listByApplicationId?applicationId=" + realm.getApplicationId(), SEE_OTHER);
    }
}
