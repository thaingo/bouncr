package net.unit8.bouncr.web.boundary;

import java.io.Serializable;

public class UserSearchParam implements Serializable {
    private Long groupId;
    private String q;
    private Integer limit = 10;
    private Integer offset = 0;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
