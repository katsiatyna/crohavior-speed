package edu.upc.bip.model;

/**
 * Created by osboxes on 16/11/16.
 */


import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements Serializable {

        private Coordinate coordinate;

        private Coordinate roundedCoordinate;

        private LocalDateTime timestamp;

        private String userID;

        public Transaction(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        public Transaction(Coordinate coordinate, LocalDateTime timestamp) {
            this.coordinate = coordinate;
            this.timestamp = timestamp;
        }

        public Transaction(Coordinate coordinate, LocalDateTime timestamp,String userID) {
            this.coordinate = coordinate;
            this.timestamp = timestamp;
            this.setUserID(userID);
        }

        public Coordinate getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        public Coordinate getRoundedCoordinate() {
            return roundedCoordinate;
        }

        public void setRoundedCoordinate(Coordinate roundedCoordinate) {
            this.roundedCoordinate = roundedCoordinate;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Transaction that = (Transaction) o;

            if (coordinate != null ? !coordinate.equals(that.coordinate) : that.coordinate != null) return false;
            if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
            if (userID != "" ? !userID.equals(that.userID) : that.userID != "") return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = coordinate != null ? coordinate.hashCode() : 0;
            result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
            return result;
        }
}

