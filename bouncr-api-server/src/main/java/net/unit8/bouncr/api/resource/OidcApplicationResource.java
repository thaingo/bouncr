package net.unit8.bouncr.api.resource;

import enkan.collection.Parameters;
import enkan.component.BeansConverter;
import enkan.security.bouncr.UserPermissionPrincipal;
import enkan.util.jpa.EntityTransactionManager;
import kotowari.restful.Decision;
import kotowari.restful.component.BeansValidator;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.bouncr.api.boundary.OidcApplicationCreateRequest;
import net.unit8.bouncr.entity.OidcApplication;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;

import java.util.Optional;
import java.util.Set;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"GET", "PUT", "DELETE"})
public class OidcApplicationResource {
    @Inject
    private BeansConverter converter;

    @Inject
    private BeansValidator validator;

    @Decision(value = MALFORMED, method = "POST")
    public Problem validateCreateRequest(OidcApplicationCreateRequest createRequest, RestContext context) {
        Set<ConstraintViolation<OidcApplicationCreateRequest>> violations = validator.validate(createRequest);
        return violations.isEmpty() ? null : Problem.fromViolations(violations);
    }

    @Decision(IS_AUTHORIZED)
    public boolean isAuthorized(UserPermissionPrincipal principal) {
        return principal != null;
    }

    @Decision(value = IS_ALLOWED, method = "GET")
    public boolean isGetAllowed(UserPermissionPrincipal principal) {
        return Optional.ofNullable(principal)
                .filter(p -> p.hasPermission("LIST_OIDC_APPLICATIONS"))
                .isPresent();
    }

    @Decision(value = IS_ALLOWED, method = "PUT")
    public boolean isPutAllowed(UserPermissionPrincipal principal) {
        return Optional.ofNullable(principal)
                .filter(p -> p.hasPermission("MODIFY_OIDC_APPLICATION"))
                .isPresent();
    }

    @Decision(value = IS_ALLOWED, method = "DELETE")
    public boolean isDeleteAllowed(UserPermissionPrincipal principal) {
        return Optional.ofNullable(principal)
                .filter(p -> p.hasPermission("DELETE_OIDC_APPLICATION"))
                .isPresent();
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params, RestContext context, EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OidcApplication> query = cb.createQuery(OidcApplication.class);
        Root<OidcApplication> oidcApplicationRoot = query.from(OidcApplication.class);
        query.where(cb.equal(oidcApplicationRoot.get("name"), params.get("name")));
        OidcApplication oidcApplication = em.createQuery(query).getResultStream().findAny().orElse(null);
        if (oidcApplication != null) {
            context.putValue(oidcApplication);
        }
        return oidcApplication != null;
    }

    @Decision(DELETE)
    public Void delete(OidcApplication oidcApplication, EntityManager em) {
        EntityTransactionManager tx = new EntityTransactionManager(em);
        tx.required(() -> em.remove(oidcApplication));
        em.detach(oidcApplication);
        return null;
    }
}
