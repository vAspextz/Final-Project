<template>
  <div class="event-container">
    <div>
      <!-- Display Event Details -->
      <div v-if="!isEditing" class="event-heading">
        <button class="btn-view-playlist-nav"><router-link to="/home" class="btn-back" id="back-home" style="text-decoration: none">Back to Home</router-link></button>
        <h2>{{ event.name }}</h2>
        <p>{{ event.description }}</p>
        <p><strong>Start Time:</strong> {{ event.startTime }}</p>
        <p><strong>End Time:</strong> {{ event.endTime }}</p>
        <p><strong>Date:</strong> {{ event.date }}</p>
        <button @click="editEvent" class="btn-view-playlist-nav">Edit Event</button>
      </div>
      <!-- Show Edit Form -->
      <EditEvent v-if="isEditing" :event="event" @save-event="saveEvent" @cancel-edit="cancelEdit" @close-edit="isEditing = false"/>
    </div>


    <h3>Playlists for {{ event.name }}</h3>
    <div class="playlist-tiles">
      <div
        v-for="playlist in playlists"
        :key="playlist.playlistId"
        class="playlist-tile"
      >
        <h4>{{ playlist.name }}</h4>
        <p>{{ playlist.description }}</p>
        <button @click="viewPlaylist(playlist.playlistId)" class="btn-view-playlist">View Playlist</button>
        <button @click="savePlaylist(playlist.playlistId)" class="btn-view-playlist">Save Playlist</button>
        <button @click="removePlaylist(playlist.playlistId)" class="btn-view-playlist">Remove Playlist</button>
      </div>
      <p v-if="playlists.length === 0">No playlists found for this event.</p>


    </div>
    <div class="button-holder">
      <button class="btn-create-playlist" v-on:click="this.showPlaylist = true">Create New Playlist</button>
      <button class="btn-create-playlist" v-on:click="displaySavedPlaylists()" type="button">Find Saved Playlist</button>
      <button @click="toggleQRCode" class="btn-create-playlist">Show QR Code</button>
    </div>
    <div class="options">
      <!-- Create Playlist Form -->
      <div class="show_playlist" v-if="showPlaylist">
        <form v-on:submit.prevent="submitForm" class="playlistForm">
          <div class="form-group">
            <label for="playlist-name" class="form-label">Choose a name for your playlist:</label>
            <input
              id="playlist-name"
              type="text"
              class="form-input"
              v-model="editPlaylist.name"
              placeholder="Enter playlist name"
              autocomplete="off"
            />
          </div>
          <div class="form-actions">
            <button class="btn btn-submit">Create Playlist</button>
            <button class="btn btn-cancel" v-on:click="cancelForm" type="button">Cancel</button>
          </div>
        </form>
      </div>
      <div v-if="showSavedPlaylists">
        <h4>Select a saved playlist to add to this event.</h4>
        <div class="playlist-tiles">
          <div v-for="playlist in savedPlaylists" :key="playlist.id" class="playlist-tile">
            <h3 class="playlist-title">{{ playlist.name }}</h3>
            <div class="playlist-buttons">
              <button class="btn-view-details" @click="addPlaylistToEvent(playlist.playlistId)">Add Playlist to Event</button>
            </div>
          </div>
        </div>
      </div>
      <div class="qr-code-container">
        <div v-if="showQRCode" class="qr-code">
          <img :src="qrCode" alt="Event QR Code" class="qr-image" />
          <button @click="downloadQRCode" class="btn-download-qr">Download QR Code</button>
        </div>
      </div>          
    </div>
   
   






   
  </div>
</template>
<script>
import axios from 'axios';
import EventService from '../services/EventService';
import EditEvent from '../components/EditEvent.vue';


export default {
  components: {
    EditEvent
  },
  data() {
    return {
      event: {},
      playlists: [],
      savedPlaylists: [],
      showPlaylist: false,
      showSavedPlaylists: false,
      showQRCode: false,
      qrCode: '',
      editPlaylist: {
        name: '',
        userId: this.$store.state.user.id
      },
      isEditing: false,
    };
  },
  created() {
    this.fetchEventDetails();
    this.fetchPlaylists();
  },
  methods: {
    async fetchEventDetails() {
      try {
        const eventId = this.$route.params.eventId;
        const response = await axios.get(`/events/${eventId}`);
        this.event = response.data;
        this.generateQRCode();
      } catch (error) {
        console.error("Error fetching event details:", error);
        alert("Failed to load event details.");
      }
    },
    async fetchPlaylists() {
      try {
        const eventId = this.$route.params.eventId;
        const response = await axios.get(`/events/${eventId}/playlists`);
        this.playlists = response.data;
      } catch (error) {
        if (error.response && error.response.status === 404) {
          console.warn("No playlists found for this event.");
        } else {
          console.error("Error fetching playlists:", error);
        }
      }
    },
    async fetchSavedPlaylists() {
      try {
        const userId = this.$store.state.user?.id;
        if (!userId) {
          console.warn("User ID is not defined.");
          return;
        }
        const response = await axios.get(`/users/${userId}/playlists`);
        this.savedPlaylists = response.data;
      } catch (error) {
        if (error.response && error.response.status === 404) {
          console.warn("No saved playlists found for this user.");
        } else {
          console.error("Error fetching saved playlists:", error);
        }
      }
    },
    viewPlaylist(playlistId) {
      this.$router.push({ name: 'PlaylistView', params: { playlistId: playlistId, eventId: this.$route.params.eventId } });
    },
    savePlaylist(playlistId) {
      EventService
        .savePlaylist(this.$store.state.user.id, playlistId)
        .then(response => {
          if (response.status === 201) {
            console.log(response.data);
          }
        })
        .catch(error => {
          console.error('Error saving playlist:', error);
        });
    },
    displaySavedPlaylists() {
      this.showSavedPlaylists = true;
      this.fetchSavedPlaylists();
    },
    submitForm() {
      if (this.editPlaylist.name === '') {
        return;
      }
      EventService
        .addPlaylistToEvent(this.editPlaylist, this.$route.params.eventId)
        .then(response => {
          if (response.status === 201) {
            console.log(response.data);
            this.$router.push({ name: 'PlaylistView', params: { playlistId: response.data.playlistId } });
          }
        })
        .catch(error => {
          console.error('Error adding playlist:', error);
        });
      this.showPlaylist = false;
    },
    cancelForm() {
      this.showPlaylist = false;
    },
    toggleQRCode() {
      this.showQRCode = !this.showQRCode;
    },
    generateQRCode() {
      const eventId = this.$route.params.eventId;
      this.qrCode = `https://api.qrserver.com/v1/create-qr-code/?data=/events/${eventId}&size=150x150`;
    },
    downloadQRCode() {
      const link = document.createElement('a');
      link.href = this.qrCode;
      link.download = `event-${this.event.name}-qr-code.png`;
      link.click();
    },
    async removePlaylist(playlistId) {
      var result = confirm("Are you sure you want to remove this playlist from the event?");
      if (result) {
        try {
          this.playlists = this.playlists.filter(playlist => playlist.id !== playlistId);
          const eventId = this.$route.params.eventId;
          const response = await axios.delete(`/playlists/${playlistId}/events/${eventId}`);
          if (response.status === 204) {
            await this.fetchPlaylists();
            window.location.reload();
          } else {
            alert('Unexpected response from the server.');
          }
        } catch (error) {
          console.error('Failed to remove the event from the playlist:', error);
          alert('An error occurred while trying to remove the event from the playlist. Please try again.');
        }
      }
    },
    async addPlaylistToEvent(playlistId) {
      const eventId = this.$route.params.eventId
      try {
        // Make the POST request to link the playlist to the event
        const response = await axios.post(`http://localhost:9000/events/${eventId}/playlists/${playlistId}`);


        // Check the response status and handle success
        if (response.status === 201) {
          console.log('Playlist linked successfully:', response.data);
          this.fetchPlaylists();
          // Optionally redirect to a different view or take another action
          // this.$router.push({ name: 'PlaylistView', params: { playlistId: response.data.playlistId } });
        }
      } catch (error) {
        // Handle errors
        console.error('Error adding playlist:', error);
        if (error.response) {
          console.error('Server responded with:', error.response.status, error.response.data);
        } else if (error.request) {
          console.error('No response received:', error.request);
        } else {
          console.error('Error during setup:', error.message);
        }
      }
    },
    // Show the edit form
    editEvent() {
      this.isEditing = true;
    },
    // Save the edited event
    async saveEvent(updatedEvent) {
      updatedEvent.creator = this.$store.state.user.id;
      //updatedEvent.date = updatedEvent.date.toISOString().split('T')[0];
      console.log(updatedEvent);
      try {
        const eventId = this.$route.params.eventId;  // Get the event ID from the route
        const response = await axios.put(`/events/${eventId}`, updatedEvent);




        if (response.status === 204) {
          // Successfully updated the event in the database
          this.$emit('save-event', updatedEvent);  // Pass the updated event back to the parent component
          //alert('Event updated successfully!');
          this.$router.push({ name: 'EventView', params: { eventId: eventId } });  // Redirect to the event view page
          await this.fetchEventDetails();
        } else {
          alert('Failed to update event.');
        }
      } catch (error) {
        console.error('Error updating event:', error);
        alert('An error occurred while updating the event. Please try again.');
      }
    },
    // Reset the form if cancel is clicked
    cancelEdit(originalEvent) {
      this.event = { ...originalEvent }; // Revert to original event data
      this.isEditing = false;  // Exit edit mode
    },


  }
};
</script>
<style scoped>
.button-holder{
  display: flex;
  flex-direction:row;
  justify-content: space-around;
  align-items: baseline;
  margin-right: 10%;
  margin-left: 10%;
}
.event-heading {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}
.event-container {
  text-align: center;
  font-size: 2rem;
}
/* Improved Playlist Form Styling */
.playlistForm {
  margin: 20px auto;
  padding: 20px;
  background: #333; /* Dark grey background */
  border-radius: 10px;
  width: 400px;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.5);
  text-align: center;
}

.form-group {
  margin-bottom: 15px;
}

.form-label {
  display: block;
  font-size: 1.2rem;
  color: #fff; /* White text */
  margin-bottom: 10px;
}

.form-input {
  width: 100%;
  padding: 10px;
  border-radius: 5px;
  border: 1px solid #aaa; /* Light grey border */
  font-size: 1rem;
  background: #222; /* Dark background */
  color: #fff; /* White text */
  box-sizing: border-box; /* Include padding and border in the width */
}

.form-input:focus {
  outline: none;
  border-color: rgb(255, 105, 180, 0.9); /* Purple border on focus */
  box-shadow: 0px 0px 8px rgb(255, 105, 180, 0.6); /* Purple shadow on focus */
}

.form-actions {
  display: flex;
  justify-content: space-between;
}

.btn {
  font-size: 1rem;
  padding: 0.7rem 1.5rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:hover {
  background: rgb(255, 105, 180, 0.6); /* Darker purple on hover */
}

.btn-submit {
  background: rgb(255, 105, 180, 0.9); /* Purple background */
  color: #fff; /* White text */
}

.btn-submit:hover {
  background: rgb(255, 105, 180, 0.6); /* Darker purple for submit on hover */
}

.btn-cancel {
  background: rgb(255, 105, 180, 0.9); /* Lighter purple for cancel */
  color: #fff; /* White text */
}

.btn-cancel:hover {
  background: rgb(255, 105, 180, 0.6); /* Darker purple for cancel on hover */
}
/* Other Existing Styling */
.playlist-tiles {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  justify-content: center;
}
.playlist-tile {
  background: linear-gradient(145deg,  rgb(255, 105, 180, 0.9), #0E0101);
  border: 2px solid #ddd;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 5px 5px 15px rgba(0, 0, 0, 0.2);
  width: 320px;
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
}
/* .playlist-tile:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 10px 10px 30px rgba(0, 0, 0, 0.4);
  background: linear-gradient(145deg, #F39C12, #C0392B);
} */
.playlist-tile h4 {
  margin: 0;
  font-size: 2.5rem;
}
.playlist-tile p {
  font-size: 1.8rem;
  color: #555;
}
.playlist-name {
  width:50%;
}
.btn-create-playlist {
  /*background-color: #8E44AD;*/
  background: linear-gradient(45deg, rgb(255, 105, 180, 0.9), #0A050C);
  box-shadow: 0 6px 12px rgb(255, 255, 255);
  color: white;
  padding: 15px 30px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 2rem;
  transition: transform 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
  margin-top: 20px;
}

 
.btn-create-playlist:hover {
  background-color: #4F033A;
  transform: scale(1.1);
  box-shadow: 0 0 15px white;
}
.btn-view-playlist {
  background-color: rgb(14, 13, 13);
  color: rgb(255, 105, 180, 0.9);
  border: 2px solid rgb(255, 105, 180, 0.9);
  padding: 18px 28px; /* 45% bigger */
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease;
  /*display: flex;
  flex-direction: column;
  
  justify-content: center;*/
  width: 100%;
  text-align: center;
}
.btn-view-playlist:hover {
  background-color:  rgb(255, 105, 180, 0.9);
  color: white;
}
.btn-view-playlist-nav {
  background-color: rgb(14, 13, 13);
  color: rgb(255, 105, 180, 0.9);
  border: 2px solid rgb(255, 105, 180, 0.9);
  padding: 18px 28px; /* 45% bigger */
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease;
  display: flex;
  flex-direction: column;
  
  justify-content: center;
}
.btn-view-playlist-nav:hover {
  background-color:  rgb(255, 105, 180, 0.9);
  color: white;
}

#back-home {
  color: rgb(255, 105, 180, 0.9)
}
#back-home:hover {
  color: white
}
.btn-qr-code {
  background-color: #F39C12;
  color: white;
  padding: 15px 30px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 2rem;
  transition: background-color 0.3s ease, transform 0.3s ease, box-shadow 0.3s ease;
}
.btn-qr-code:hover {
  background-color: #E67E22;
  transform: scale(1.1);
  box-shadow: 0 0 20px #E67E22;
}
.qr-code-container {
  margin-top: 20px;
}
.qr-image {
  width: 200px;
  height: 200px;
  margin-bottom: 20px;
}
.btn-download-qr {
  background: linear-gradient(45deg, rgb(255, 105, 180, 0.9), #0A050C);
  box-shadow: 0 6px 12px rgb(255, 255, 255);
  color: white;
  padding: 15px 30px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 2rem;
  transition: transform 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
  margin-top: 20px;
}
.btn-download-qr:hover {
  background-color: #27AE60;
  transform: scale(1.1);
}
.btn-find {
  padding: 20px;
  margin: 15px;
  height: 30px;
  width: 250px;
  color:white;
  background-color: #8E44AD;


}
</style>
