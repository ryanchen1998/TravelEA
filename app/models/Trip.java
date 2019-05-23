package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.Model;

import java.util.Comparator;
import java.util.List;
import javax.persistence.*;

import play.data.validation.Constraints;

/**
 * A class that models the trip database table.
 */
@Entity
@Table(name = "Trip")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trip extends Model implements Comparable<Trip> {

    @Id
    public Long id;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    @Column(name = "is_public")
    public boolean isPublic;

    @OneToMany(cascade = CascadeType.ALL)
    public List<TripData> tripDataList;

    /**
     * Finds the first date in a trip, if there is one.
     *
     * @return the date found  as a string or null if no date was found
     */
    public String findFirstTripDate() {
        for (TripData tripData : tripDataList) {
            if (tripData.arrivalTime != null) {
                return tripData.arrivalTime.toString().substring(0, 10);
            } else if (tripData.departureTime != null) {
                return tripData.departureTime.toString().substring(0, 10);
            }
        }

        return null;
    }

    /**
     * Comparator which allows for trips to be compared and sorted by date
     * Will sort by recent first, with nulls last
     *
     * @param other Trip to compare against
     * @return Negative or zero integer if this trip should be first, otherwise positive
     */
    @Override
    public int compareTo(Trip other) {
        if (other.findFirstTripDate() == null) {
            return -1;
        } else if (this.findFirstTripDate() == null) {
            return 1;
        } else {
            return other.findFirstTripDate().compareTo(this.findFirstTripDate());
        }
    }
}
