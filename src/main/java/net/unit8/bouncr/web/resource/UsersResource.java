package net.unit8.bouncr.web.resource;

import enkan.component.BeansConverter;
import enkan.data.DefaultHttpRequest;
import enkan.util.jpa.EntityTransactionManager;
import kotowari.restful.Decision;
import kotowari.restful.component.BeansValidator;
import kotowari.restful.data.DefaultResouruce;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.bouncr.web.boundary.CreateUserRequest;
import net.unit8.bouncr.web.boundary.UserSearchParam;
import net.unit8.bouncr.web.entity.Group;
import net.unit8.bouncr.web.entity.User;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

import static kotowari.restful.DecisionPoint.HANDLE_OK;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

@AllowedMethods({"GET", "POST"})
public class UsersResource {
    @Inject
    private BeansConverter converter;

    @Inject
    private BeansValidator validator;

    @Decision(value = MALFORMED, method = "POST")
    public Problem validateUserCreateRequest(CreateUserRequest createRequest, RestContext context) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(createRequest);
        if (violations.isEmpty()) {
            context.putValue(converter.createFrom(createRequest, User.class));
        }
        return violations.isEmpty() ? null : Problem.fromViolations(violations);
    }

    @Decision(HANDLE_OK)
    public List<User> handleOk(UserSearchParam params, EntityManager em) {
        new RestContext(new DefaultResouruce(), new DefaultHttpRequest());
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        if (params.getGroupId() != null) {
            Join<Group, User> groups = root.join("groups", JoinType.LEFT);
            query.where(builder.equal(groups.get("id"), params.getGroupId()));
        }
        return em.createQuery(query)
                .setFirstResult(params.getOffset())
                .setMaxResults(params.getLimit())
                .getResultList();
    }

    @Decision(POST)
    public User doPost(User user, EntityManager em) {
        EntityTransactionManager tm = new EntityTransactionManager(em);
        tm.required(() -> em.persist(user));
        return user;
    }
}
