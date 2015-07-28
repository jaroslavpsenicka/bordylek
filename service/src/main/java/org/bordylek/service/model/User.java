package org.bordylek.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;

@Document(collection = "user")
public class User implements Unique {

	@Id
	private String id;

	@NotNull (message="regId may not be null")
	@Indexed(unique = true, sparse = true)
	@JsonIgnore
	private String regId;
		
	@NotNull (message="name may not be null")
	private String name;
	
	@Email
	private String email;
	private String locale;
	private UserStatus status;

	@NotNull (message="reg may not be null")
	@JsonIgnore
	private Registrar reg;
	@JsonIgnore
	private boolean deleted;
	@JsonIgnore
	private boolean disabled;
	
	@NotNull
	@JsonIgnore
	private Date createDate;
	private String imageUrl;
	private String location;

	private String[] roles;
	
	public User() {
	}
	
	public User(String name, String email, String... roles) {
		this.name = name;
		this.email = email;
		this.roles = roles;
		this.reg = Registrar.GOOGLE;
		this.status = UserStatus.INCOMPLETE;
	}

	public User(String name, String email, Collection<String> roles) {
		this.name = name;
		this.email = email;
		this.roles = roles.toArray(new String[roles.size()]);
		this.reg = Registrar.GOOGLE;
		this.status = UserStatus.INCOMPLETE;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Date getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Registrar getReg() {
		return reg;
	}

	public void setReg(Registrar reg) {
		this.reg = reg;
	}

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}
	
	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

}
