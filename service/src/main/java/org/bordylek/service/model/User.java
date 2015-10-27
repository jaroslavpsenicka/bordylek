package org.bordylek.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "user")
public class User implements Unique {

	@Id
	private String id;

	@NotEmpty(message = "regId may not be null")
	@Indexed(unique = true, sparse = true)
	@JsonIgnore
	private String regId;
		
	@NotEmpty(message = "name may not be null")
    @Length(min = 3, max = 255, message = "name should be 3-255 characters long")
	private String name;
	
	@Email(message = "wrong email format")
    @NotEmpty(message = "email may not be null")
    private String email;

	private String locale;
	private UserStatus status;

	@NotNull
	@JsonIgnore
	private Date createDate;
	private String imageUrl;
	private Location location;

	@Valid
	private List<CommunityRef> communities = new ArrayList<>();

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

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}

	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public List<CommunityRef> getCommunities() {
		return communities;
	}

	public void setCommunities(List<CommunityRef> communities) {
		this.communities = communities;
	}

	public boolean isMemberOf(Community community) {
		Assert.notNull(community);
		for (CommunityRef ref : this.communities) {
			if (community.getId().equals(ref.getId())) {
				return true;
			}
		}

		return false;
	}
}
