# Synesthesia Vision

Este projeto visa o desenvolvimento de óculos sensoriais para deficientes visuais com IoT e capacidade de se conectar a cidades inteligentes. Através de sensores ultrasônicos, a distância dos objetos até o usuário é mapeada e enviada para um smartphone (usando um aplicativo), processada e transmitida através de som 3D através de fones de ouvido. Além deste recurso de sonificação, outras capacidades de IoT podem ser adicionadas graças ao aplicativo e ao módulo NRF24 nos óculos.

## Mais informações
[Vídeo no Youtube](https://www.youtube.com/watch?v=zJbMHiAeXqk)

[Página no Facebook](https://www.facebook.com/synesthesiavision/?fref=ts)

## Layout do projeto
Esse projeto consiste de quatro principais componentes:
1. Óculos com Arduino
2. Smartphone App
3. Web Server
4. Dispositivos IoT

### Óculos com Arduino
O Código do Arduino e o layout da PCB para os componentes são encontrados na pasta "Glasses_Arduino".


O Arduino utilizado é um Pro_Mini e ele é conectado a 3 __módulos__;
1. Sensores Ultrasônicos;
2. Módulo Bluetooth;
3. Módulo NRF24.

Para a criação do som, o Arduino recebe a distância dos sensores ultrasônicos, filtra essa informação e envia ao Smartphone através do __módulo Bluetooth__.

O módulo __NRF24__ e o Smartphone permitem ao Synesthesia Vision conectar-se a outros dispositivos IoT presentes em cidades inteligentes.

### Smartphone App
O Aplicativo para Android pode ser encontrado na pasta "Synesthesia_APP", o app é desenvolvido com o Android Studio, IDE da Google.

O app auxilia o recurso de sonificação, fornece ao usuário feedback de áudio para os recursos acionados pelo NRF24 e se conecta com o servidor Web para fornecer capacidades IoT.

Mais informações sobre o app do Smartphone podeser achado na pasta "Synesthesia_APP"

### Web Server e IoT
O Servidor em NodeJS pode ser achado na pasta "Server".

O Smartphone é capaz de se conectar com o servidor e receber e enviar requisições para recursos encontrados na internet, como receber a previsão do tempo.

Mais informações sobre o servidor e a lista de serviços IoT podem ser encontrados na pasta "Server".

### Smart City Devices

Pretendemos construir uma ferramenta útil para que os deficientes visuais interajam com as atividades do dia-a-dia com relativa facilidade. Isto pode ser feito com a integração com dispositivos IoT, tais como: paradas de ônibus e semáforos.

## Autores

* **Michael Barney Jr**
* **Jonathan Kilner**
* **Gilmar Brito**
* **Aida Araújo Ferreira** 

Veja também a lista de [contribuintes](https://github.com/aidaferreira/synesthesiavision/contributors) que participaram desse projeto.

## Licença

Este projeto é licenciado sob a licença GNU General Public License v3.0 - consulte o arquivo [LICENSE](LICENSE) para obter detalhes

## Agradecimentos

Esse projeto foi desenvolvido em parceria com o Instituto Federal de Ciência e Tecnologia de Pernambuco:

![alt text](http://3.bp.blogspot.com/_L5KqKS1TcJg/TK8Qe3nMK2I/AAAAAAAAPz8/8UO_DPI0bXM/s1600/IFPE%2520foto.png "IFPE")

