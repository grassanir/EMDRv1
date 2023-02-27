/*  Programa para ESP32 antes da atualização OTA */
#include <WiFi.h>
#include <WiFiClient.h>
#include <WebServer.h>
#include <ESPmDNS.h>
#include <Update.h>
#include <RCSwitch.h>

RCSwitch mySwitch = RCSwitch();
 
/* Constantes - conexão wi-fi e webserver */
const char* host = "esp32";
const char* ssid = "Rafael"; /* coloque aqui o nome da rede wi-fi que o ESP32 deve se conectar */
const char* password = "peperoni"; /* coloque aqui a senha da rede wi-fi que o ESP32 deve se conectar */

IPAddress ip(192,168,50,175); //COLOQUE UMA FAIXA DE IP DISPONÍVEL DO SEU ROTEADOR. EX: 192.168.1.110 **** ISSO VARIA, NO MEU CASO É: 192.168.0.175
IPAddress gateway(192,168,50,1); //GATEWAY DE CONEXÃO (ALTERE PARA O GATEWAY DO SEU ROTEADOR)
IPAddress subnet(255,255,255,0); //MASCARA DE REDE
 
/* Variáveis globais */
int contador_ms = 0;
 
/* Webserver para se comunicar via browser com ESP32  */
WebServer server(80);
 
/* Códigos da página que será aberta no browser 
   (quando comunicar via browser com o ESP32) 
   Esta página exigirá um login e senha, de modo que somente 
   quem tenha estas informações consiga atualizar o firmware
   do ESP32 de forma OTA */
const char* loginIndex = 
"<style>"
"body {background-color: #17B1E7;}"
"</style>"
  
 "<form name='loginForm'>"
    "<table width='20%' bgcolor='17B1E7' align='center'>"
        "<tr>"
            "<td colspan=2>"
                "<img src='https://github.com/grassanir/EMDRv1/blob/master/logo22.png?raw=true'>"
                "<br>"
                "<br>"
            "</td>"
            "<br>"
            "<br>"
        "</tr>"
        "<td>Usu&aacuterio:</td>"
        "<td><input type='text' size=25 name='userid'><br></td>"
        "</tr>"
        "<br>"
        "<br>"
        "<tr>"
            "<td>Senha:</td>"
            "<td><input type='Password' size=25 name='pwd'><br></td>"
            "<br>"
            "<br>"
        "</tr>"
        "<tr>"
            "<td><input type='submit' onclick='check(this.form)' value='Acesso'></td>"
        "</tr>"
    "</table>"
"</form>"
"<script>"
    "function check(form)"
    "{"
    "if(form.userid.value=='admin' && form.pwd.value=='admin')"
    "{"
    "window.open('/serverIndex')"
    "}"
    "else"
    "{"
    " alert('Login ou senha inválidos')"
    "}"
    "}"
"</script>";
  
const char* serverIndex = 
  "<style>"
"body {background-color: #17B1E7;}"
"</style>"
"<br>"
"<br>"
"<br>"
"<br>"
"<br>"
"<br>"

"<center><img src='https://github.com/grassanir/EMDRv1/blob/master/logo22.png?raw=true'></center>"

"<script src='https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js'></script>"
"<center><form method='POST' action='#' enctype='multipart/form-data' id='upload_form'>"
      "<br>"
   "<input type='file' name='update'>"
           "<br>"
           "<br>"
        "<input type='submit' value='Update'>"
    "</form></center>"
 "<center><div id='prg'>Progresso: 0%</div></center>"
 "<script>"
  "$('form').submit(function(e){"
  "e.preventDefault();"
  "var form = $('#upload_form')[0];"
  "var data = new FormData(form);"
  " $.ajax({"
  "url: '/update',"
  "type: 'POST',"
  "data: data,"
  "contentType: false,"
  "processData:false,"
  "xhr: function() {"
  "var xhr = new window.XMLHttpRequest();"
  "xhr.upload.addEventListener('progress', function(evt) {"
  "if (evt.lengthComputable) {"
  "var per = evt.loaded / evt.total;"
  "$('#prg').html('Progresso: ' + Math.round(per*100) + '%');"
  "}"
  "}, false);"
  "return xhr;"
  "},"
  "success:function(d, s) {"
  "console.log('Sucesso!')"
 "},"
 "error: function (a, b, c) {"
 "}"
 "});"
 "});"
 "</script>";
 

 
void setup(void) 
{
    Serial.begin(115200);
    mySwitch.enableReceive(27);  // Receiver on interrupt 0 => that is pin #2
  pinMode(2, OUTPUT);
 
    /* Conecta-se a rede wi-fi */
    WiFi.begin(ssid, password);
    WiFi.config(ip, gateway, subnet); //PASSA OS PARÂMETROS PARA A FUNÇÃO QUE VAI SETAR O IP FIXO NO NODEMCU
    Serial.println("");
 
    while (WiFi.status() != WL_CONNECTED) 
    {
        delay(500);
        Serial.print(".");
    }
     
    Serial.println("");
    Serial.print("Conectado a rede wi-fi ");
    Serial.println(ssid);
    Serial.print("IP obtido: ");
    Serial.println(WiFi.localIP());
 
    /* Usa MDNS para resolver o DNS */
    if (!MDNS.begin(host)) 
    { 
        //http://esp32.local
        Serial.println("Erro ao configurar mDNS. O ESP32 vai reiniciar em 1s...");
        delay(1000);
        ESP.restart();        
    }
   
    Serial.println("mDNS configurado e inicializado;");
   
    /* Configfura as páginas de login e upload de firmware OTA */
    server.on("/", HTTP_GET, []() 
    {
        server.sendHeader("Connection", "close");
        server.send(200, "text/html", loginIndex);
    });
     
    server.on("/serverIndex", HTTP_GET, []() 
    {
        server.sendHeader("Connection", "close");
        server.send(200, "text/html", serverIndex);
    });
   
    /* Define tratamentos do update de firmware OTA */
    server.on("/update", HTTP_POST, []() 
    {
        server.sendHeader("Connection", "close");
        server.send(200, "text/plain", (Update.hasError()) ? "FAIL" : "OK");
        ESP.restart();
    }, []() {
        HTTPUpload& upload = server.upload();
         
        if (upload.status == UPLOAD_FILE_START) 
        {
            /* Inicio do upload de firmware OTA */
            Serial.printf("Update: %s\n", upload.filename.c_str());
            if (!Update.begin(UPDATE_SIZE_UNKNOWN)) 
                Update.printError(Serial);
        } 
        else if (upload.status == UPLOAD_FILE_WRITE) 
        {
            /* Escrevendo firmware enviado na flash do ESP32 */
            if (Update.write(upload.buf, upload.currentSize) != upload.currentSize) 
                Update.printError(Serial);      
        } 
        else if (upload.status == UPLOAD_FILE_END) 
        {
            /* Final de upload */
            if (Update.end(true))             
                Serial.printf("Sucesso no update de firmware: %u\nReiniciando ESP32...\n", upload.totalSize);
            else
                Update.printError(Serial);
        }   
    });
    server.begin();
}
 
void loop() 
{

  if (mySwitch.available()) {
    
    Serial.print("Received ");
    Serial.print( mySwitch.getReceivedValue() );
    Serial.print(" / ");
    Serial.print( mySwitch.getReceivedBitlength() );
    Serial.print("bit ");
    Serial.print("Protocol: ");
    Serial.println( mySwitch.getReceivedProtocol() );
    if (mySwitch.getReceivedValue() == 14168321)
        digitalWrite(2, HIGH);
    if (mySwitch.getReceivedValue() == 14168325)
        digitalWrite(2, LOW);

    mySwitch.resetAvailable();

    
        
  }
    server.handleClient();
    delay(1);
 
    contador_ms++;
 
    if (contador_ms >= 1000)
    {    
        Serial.println("Programa antes da atualizacao OTA");
        contador_ms = 0;
    }
}
