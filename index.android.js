import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  TouchableOpacity
} from 'react-native';

import Location from './src/services/LocationApi';
import BackgroundLocation from './src/services/BackgroundLocation';

export default class LocationApi extends Component {

  startBackgroundService() {
    //Init Background Location Service with URL to send position.
    BackgroundLocation.init('http://localhost:8080/');
  }

  startLocation() {
    //Initialize Location API 
    Location.init();
  }

  getLocation() {
    Location.getLocation((lat, lng) => {
      console.warn('lat:' + lat +' | lng: ' + lng);
    });
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Location API
        </Text>
        <TouchableOpacity onPress={this.startBackgroundService}>
          <Text style={styles.instructions}>
            Start Background service
          </Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.startLocation}>
          <Text style={styles.instructions}>
            Start Location API service
          </Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.getLocation}>
          <Text style={styles.instructions}>
            Get position
          </Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('LocationApi', () => LocationApi);
