# Synesthesia Vision

This project aims to develop pairs of sensory glasses for the visually impaired with IoT and Smart City Capabilities. Through and array of Ultrassonic Sensors, 3D distance information is mapped, sent to a smartphone (using an app), processed and transmitted through 3D sound via headphones. Beyond this sonification feature, other IoT and Smart City Capabilities can be added thanks to the app and the NRF24 module on the glasses.

## More Information
[Youtube Video](https://www.youtube.com/watch?v=zJbMHiAeXqk)

[Facebook Page](https://www.facebook.com/synesthesiavision/?fref=ts)

## Layout of the Project
This project consists of 4 main components:
1. Glasses with Arduino
2. Smartphone App
3. Web Server
4. IoT and Smart City Devices

### Glasses with Arduino
The Arduino Code and PCB layouts for this component are found in the "Glasses_Arduino" folder.

The Arduino used is the Pro_Mini, and it is connected to 3 __modules__:
1. Array of Ultrassonic Sensors
2. Smartphone Comunication Module
3. Remote Comunication Module

For the sonification, the Arduino receives distance information from a __Array of Ultrassonic Sensors__, it filters these information and sends it to the Smartphone through the __Bluetooth Module__.

Both the __NRF24__ and the Smartphone allow Synesthesia Vision to connect with other IoT and Smart City devices.

### Smartphone App
The Android App can be found in the "Synesthesia_APP" folder, the app is developed on Android Studio.

The app assists the sonification feature, supplies the user with audio feedback for the NRF24 triggered features and connects with the Web Server to supply IoT capabilities.

More information about the Smartphone App can be found in the App Folder.

### Web Server and ioT
The NodeJS Server app can be found in the "Server" folder.

The server connects with the smartphone, sendind and receiving requests for features found in the internet, such as retreiving weather information.

More information about the Server App and a list of IoT features can be found in the Server folder.

### Smart City Devices
We aim to construct a porwerfull tool for the visually impaired to interact with day-to-day activities with relative ease. This can be done with the integration with Smart City Devices, such as: bus stops and traffic lights. 

## Authors

* **Michael Barney Jr**
* **Jonathan Kilner**
* **Gilmar Brito**
* **Aida Araújo Ferreira** 

See also the list of [contributors](https://github.com/aidaferreira/synesthesiavision/contributors) who participated in this project.

## License

This project is licensed under the GNU General Public License v3.0 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project was developped in association with the Pernambuco Institute of Science Education and Technology:

![alt text](http://3.bp.blogspot.com/_L5KqKS1TcJg/TK8Qe3nMK2I/AAAAAAAAPz8/8UO_DPI0bXM/s1600/IFPE%2520foto.png "IFPE")

