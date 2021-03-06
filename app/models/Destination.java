package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import play.data.validation.Constraints;

/**
 * This class models the destination table in the database, with all requirements met. A finder is
 * also supplied for easy and concise queries.
 */
@Entity
@Table(name = "Destination")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Destination extends BaseModel implements Taggable {

    @Id
    @Constraints.Required
    public Long id;

    @ManyToOne
    @Constraints.Required
    public User user;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Column(name = "type") //type is a keyword in scala so cant get the column
    public String destType;

    @Constraints.Required
    public String district;

    @Constraints.Required
    public Double latitude;

    @Constraints.Required
    public Double longitude;

    @Constraints.Required
    @Column(name = "is_public")
    public boolean isPublic;

    @ManyToOne
    @Constraints.Required
    public CountryDefinition country;

    @ManyToOne
    @Column(name = "primary_photo_guid")
    public Photo primaryPhoto;

    @JsonIgnore
    @ManyToMany(mappedBy = "destinationPhotos")
    @JoinTable(
        name = "DestinationPhoto",
        joinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "guid"))

    public List<Photo> destinationPhotos;

    @ManyToMany(mappedBy = "destTravellerTypes")
    @JoinTable(
        name = "DestinationTravellerType",
        joinColumns = @JoinColumn(name = "dest_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "traveller_type_definition_id", referencedColumnName = "id"))
    public List<TravellerTypeDefinition> travellerTypes;

    @ManyToMany(mappedBy = "destTravellerTypesPending")
    @JoinTable(
        name = "DestinationTravellerTypePending",
        joinColumns = @JoinColumn(name = "dest_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "traveller_type_definition_id", referencedColumnName = "id"))
    public List<TravellerTypeDefinition> travellerTypesPending;

    @ManyToMany(mappedBy = "destinations")
    @JoinTable(
        name = "DestinationTag",
        joinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
    public Set<Tag> tags;

    @ManyToMany(mappedBy = "destinationPrimaryPhotos")
    @JoinTable(
            name = "PendingDestinationPhoto",
            joinColumns = @JoinColumn(name = "dest_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "photo_guid", referencedColumnName = "guid"))
    public List<Photo> pendingPrimaryPhotos;

    @JsonInclude
    @Transient
    public Long followerCount;

    /**
     * Returns the list of tags associated with the object
     *
     * @return a list of Tags
     */
    @JsonIgnore
    public Set<Tag> getTagsSet() {
        return tags;
    }

    /**
     * Returns latitude rounded to 2 dp
     *
     * @return Rounded latitude value
     */
    @JsonIgnore
    public Double getFormattedLatitude() {
        if (this.latitude != null) {
            return Math.round(this.latitude * 100) / 100.0;
        } else {
            return null;
        }
    }

    /**
     * Returns longitude rounded to 2dp
     *
     * @return Rounded longitude value
     */
    @JsonIgnore
    public Double getFormattedLongitude() {
        if (this.longitude != null) {
            return Math.round(this.longitude * 100) / 100.0;
        } else {
            return null;
        }
    }

    /**
     * Checks if photo is linked to destination.
     *
     * @param photoId id of destination of id
     * @return True if dest is linked to photo
     */
    public Boolean isLinked(Long photoId) {
        Iterator<Photo> iterator = destinationPhotos.iterator();
        while (iterator.hasNext()) {
            Photo photo = iterator.next();
            if (photo.guid.equals(photoId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if traveller type is linked to destination.
     *
     * @param travellerTypeId id of destination of id
     * @return True if dest is linked to photo
     */
    public Boolean isLinkedTravellerType(Long travellerTypeId) {
        Iterator<TravellerTypeDefinition> iterator = travellerTypes.iterator();
        while (iterator.hasNext()) {
            TravellerTypeDefinition travellerType = iterator.next();
            if (travellerType.id.equals(travellerTypeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if traveller type is pending to be linked or unlinked from a destination
     *
     * @param travellerTypeId id of destination of id
     * @return True if dest is linked to photo
     */
    public Boolean isPendingTravellerType(Long travellerTypeId) {
        Iterator<TravellerTypeDefinition> iterator = travellerTypesPending.iterator();
        while (iterator.hasNext()) {
            TravellerTypeDefinition travellerType = iterator.next();
            if (travellerType.id.equals(travellerTypeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds request to link/unlink traveller type to a destination
     *
     * @param travellerTypeId The id of the traveller type to add
     */
    public void addPendingTravellerType(Long travellerTypeId) {
        TravellerTypeDefinition travellerTypeDefinition = new TravellerTypeDefinition();
        travellerTypeDefinition.id = travellerTypeId;
        this.travellerTypesPending.add(travellerTypeDefinition);
    }

    /**
     * Removes request to link/unlink traveller type to a destination
     *
     * @param travellerTypeId The id of the traveller type to remove
     * @return true if the traveller type was removed, false if not
     */
    public Boolean removePendingTravellerType(Long travellerTypeId) {
        Iterator<TravellerTypeDefinition> iterator = travellerTypesPending.iterator();
        while (iterator.hasNext()) {
            TravellerTypeDefinition travellerType = iterator.next();
            if (travellerType.id.equals(travellerTypeId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a photo is already pending as the destination photo
     *
     * @param photoId id of photo
     * @return True if dest is linked to photo
     */
    public Boolean hasPhotoPending(Long photoId) {
        Iterator<Photo> iterator = pendingPrimaryPhotos.iterator();
        while (iterator.hasNext()) {
            Photo photo = iterator.next();
            if (photo.guid.equals(photoId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds request to set a primary photo on the destination
     *
     * @param photoId The id of the photo to add
     */
    public void addPendingDestinationProfilePhoto(Long photoId) {
        Photo photo = new Photo();
        photo.guid = photoId;
        this.pendingPrimaryPhotos.add(photo);
    }

    /**
     * Removes request to set a primary photo on the destination
     *
     * @param photoId The id of the photo to remove
     * @return true if the photo was removed, false if not
     */
    public Boolean removePendingDestinationPrimaryPhoto(Long photoId) {
        Iterator<Photo> iterator = pendingPrimaryPhotos.iterator();
        while (iterator.hasNext()) {
            Photo photo = iterator.next();
            if (photo.guid.equals(photoId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
}
