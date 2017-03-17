# Synesthesia Vision

This project aims to develop pairs of sensory glasses for the visually impaired with IoT and Smart City Capabilities

## More Information
[Youtube Vídeo](https://www.youtube.com/watch?v=zJbMHiAeXqk)
<a href="http://www.youtube.com/watch?feature=player_embedded&v=zJbMHiAeXqk
" target="_blank"><img src="http://img.youtube.com/vi/zJbMHiAeXqk/0.jpg" 
alt="Syneshtesia Vision - Promotional Vídeo" width="240" height="180" border="10" /></a>

[Facebook Page](https://www.facebook.com/synesthesiavision/?fref=ts)

### Layout of the Project
This project consists of 4 main components:
1. Glasses with Arduino
2. Smartphone App
3. Web Server
4. IoT and Smart City Devices

### Glasses with Arduino
The Arduino Code and PCB layouts for this component are found in the "Glasses_Arduino" folder.

The Arduino used is the Pro_Mini, and it is connected to 3 __modules__:
1. Array of Ultrassonic Sensors
2. Bluetooth Module
3. NRF24 Module

For the sonification, the Arduino receives distance information from a __Array of Ultrassonic Sensors__, it filters these information and sends it to the Smartphone through the __Bluetooth Module__.

Both the __NRF24__ and the Smarphone allow Synesthesia Vision to connect with other IoT and Smart City devices.

## Smartphone App

### Web Server

### ioT and Smart City Devices

## Authors

* **Michael Barney Jr** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)
* **Jonathan Kilner** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)
* **Aida Araújo Ferreira** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)
* **Caio Moreira Gomes** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

See also the list of [contributors](https://github.com/aidaferreira/synesthesiavision/contributors) who participated in this project.

## License

This project is licensed under the GNU General Public License v3.0 License - see the [LICENSE](LICENSE) file for details

## Acknowledgments

