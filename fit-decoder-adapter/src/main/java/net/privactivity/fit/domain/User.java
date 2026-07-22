package net.privactivity.fit.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;

public class User {
    private String id;
    private String username;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String[] roles;

    private String firstName;
    private String lastName;
    private Short maxHeartRate;

    public User(String id, String username, String password, String[] roles, String firstName, String lastName,
                Short maxHeartRate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.firstName = firstName;
        this.lastName = lastName;
        this.maxHeartRate = maxHeartRate;
    }

    public User() {
    }

    public String getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String[] getRoles() {
        return this.roles;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public Short getMaxHeartRate() {
        return this.maxHeartRate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMaxHeartRate(Short maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (!Objects.equals(this$id, other$id)) {
            return false;
        }
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (!Objects.equals(this$username, other$username)) {
            return false;
        }
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        if (!Objects.equals(this$password, other$password)) {
            return false;
        }
        if (!java.util.Arrays.deepEquals(this.getRoles(), other.getRoles())) {
            return false;
        }
        final Object this$firstName = this.getFirstName();
        final Object other$firstName = other.getFirstName();
        if (!Objects.equals(this$firstName, other$firstName)) {
            return false;
        }
        final Object this$lastName = this.getLastName();
        final Object other$lastName = other.getLastName();
        if (!Objects.equals(this$lastName, other$lastName)) {
            return false;
        }
        final Object this$maxHeartRate = this.getMaxHeartRate();
        final Object other$maxHeartRate = other.getMaxHeartRate();
        return Objects.equals(this$maxHeartRate, other$maxHeartRate);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof User;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $username = this.getUsername();
        result = result * PRIME + ($username == null ? 43 : $username.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getRoles());
        final Object $firstName = this.getFirstName();
        result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
        final Object $lastName = this.getLastName();
        result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
        final Object $maxHeartRate = this.getMaxHeartRate();
        result = result * PRIME + ($maxHeartRate == null ? 43 : $maxHeartRate.hashCode());
        return result;
    }

    public String toString() {
        return "User(id=" + this.getId() + ", username=" + this.getUsername() + ", firstName=" + this.getFirstName() + ", lastName=" + this.getLastName() + ", maxHeartRate=" + this.getMaxHeartRate() + ")";
    }
}