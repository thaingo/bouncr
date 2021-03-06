package net.unit8.bouncr.entity;

import com.fasterxml.jackson.annotation.*;
import net.unit8.bouncr.json.IndirectListFilter;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * @author kawasima
 */
@Entity
@Table(name = "applications")
public class Application implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    private String name;
    @JsonIgnore
    @Column(name = "name_lower")
    private String nameLower;
    private String description;

    @JsonProperty("pass_to")
    @Column(name = "pass_to")
    private String passTo;

    @JsonProperty("virtual_path")
    @Column(name = "virtual_path")
    private String virtualPath;

    @JsonProperty("top_page")
    @Column(name = "top_page")
    private String topPage;

    @JsonIgnore
    @Column(name = "write_protected")
    private Boolean writeProtected;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = IndirectListFilter.class)
    @JsonManagedReference("realms")
    private List<Realm> realms;

    public URI getUriToPass() {
        return URI.create(passTo);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameLower = Optional.ofNullable(name)
                .map(n -> n.toLowerCase(Locale.US))
                .orElse(null);
    }

    public String getNameLower() {
        return nameLower;
    }

    public void setNameLower(String nameLower) {
        this.nameLower = nameLower;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassTo() {
        return passTo;
    }

    public void setPassTo(String passTo) {
        this.passTo = passTo;
    }

    public String getVirtualPath() {
        return virtualPath;
    }

    public void setVirtualPath(String virtualPath) {
        this.virtualPath = virtualPath;
    }

    public String getTopPage() {
        return topPage;
    }

    public void setTopPage(String topPage) {
        this.topPage = topPage;
    }

    public Boolean getWriteProtected() {
        return writeProtected;
    }

    public void setWriteProtected(Boolean writeProtected) {
        this.writeProtected = writeProtected;
    }

    public List<Realm> getRealms() {
        return realms;
    }

    public void setRealms(List<Realm> realms) {
        this.realms = realms;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        return Optional.ofNullable(obj)
                .filter(o -> getClass().isInstance(o))
                .map(o -> getClass().cast(o))
                .filter(o -> getId() != null && getId().equals(o.getId()))
                .isPresent();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nameLower='" + nameLower + '\'' +
                ", description='" + description + '\'' +
                ", passTo='" + passTo + '\'' +
                ", virtualPath='" + virtualPath + '\'' +
                ", topPage='" + topPage + '\'' +
                ", writeProtected=" + writeProtected +
                '}';
    }
}
