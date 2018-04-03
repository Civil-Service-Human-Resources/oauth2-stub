package uk.gov.cshr.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 100)
    private String name;

    private String description;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parentId")
    private Role parent;

    @OneToMany(mappedBy = "parent")
    private Set<Role> children = new HashSet<>();

    protected Role() {
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Role(String name, String description, Role parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parent=" + parent +
                ", children=" + children +
                '}';
    }
}
