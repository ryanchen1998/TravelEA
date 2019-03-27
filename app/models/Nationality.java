package models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

/**
 * A class that represents a user's nationalities
 */
@Entity
@Table(name="Nationality")
public class Nationality extends Model {
    @Id
    public Long guid;

    @Constraints.Required
    public Long countryId;

    @Constraints.Required
    public Long userId;

//    @ManyToMany(mappedBy = "nationalities")
//    public List<Profile> nationalities;
}