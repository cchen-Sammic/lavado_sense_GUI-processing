//%1,eva,b_lav,c_cald,c_cuba,vaciado,b_pres,powerOn,puerta,boya_inf,boya_sup,pres_bajo,t_cuba,t_cald,t_aclarado$
//%2,spla,splb,spaa,spab,spd,scab,stca,stcu,st$
// 5 de agosto del 2016
import processing.serial.*;
import g4p_controls.*;
import org.multiply.processing.TimedEventGenerator;

private TimedEventGenerator dataRequestEvent_1;
private TimedEventGenerator dataRequestEvent_2;
private TimedEventGenerator blinkRequest;
private TimedEventGenerator blinkAnswer;

Serial myPort;

boolean requestEvent_1 = false;
boolean requestEvent_2 = false;

String serialList[];
boolean serialSelection = false;
String inString="";
String splitDataAnswer[];
boolean triggerAppend= false;

String requestDataArray_1[];
String requestDataArray_2[];
int request1ArrayPos=0;
int request2ArrayPos=0;
boolean requestDataRepresentation = false;
int IDspy;
int IDsensor;

String offSetListParameters[]={"temperatura resistencia calderin", "temperatura resistencia cuba", "presion lavado A", "presion lavado B", "presion aclarado A", "presion aclarado B", "presion dosificador", "caudal aclarado B"};
// trcucba:1, trcalderin:2, pla:3, plb:4, paa:5, pab:6, pd:7, cab:8
int IDoffset;

boolean blinkRequestLine = false;
boolean blinkAnswerLine = false;

PImage img_principal;
PImage img_eva;
PImage img_lavado;
PImage img_aclarado;
PImage img_vaciado;
PImage img_presion;
PImage img_boya_inf;
PImage img_boya_sup;
PImage img_pres_bajo;
PImage img_puerta_abierta;
PImage img_puerta_cerrada;
PImage img_c_cuba;
PImage img_c_calderin;
PImage img_botonOn;
PImage img_botonOff;
PImage img_logoSammic;
PImage logoIcon;
int ix = 650;
int iy = 350;
int posx = 200;
int posy = 45;

boolean stateEva = false;
boolean stateB_lav = false;
boolean stateC_cald = false;
boolean stateC_cuba = false;
boolean stateVaciado = false;
boolean stateB_pres = false;
boolean statePowerOn = false;
boolean statePuerta = false;
boolean stateBoya_inf = false;
boolean stateBoya_sup = false;
boolean statePres_bajo = false;
float stateT_cuba = 0;
float stateT_cald = 0;
float stateT_aclarado = 0;


void setup() {
  frameRate(15);
  size(1050, 500);
  logoIcon = loadImage("icono.png");
   surface.setIcon(logoIcon);

 
 
  serialList = Serial.list(); 

  requestDataArray_1 = new String[30];
  requestDataArray_2 = new String[30];

  dataRequestEvent_1 = new TimedEventGenerator(this, "onDataRequestEvent_1", false);
  dataRequestEvent_1.setIntervalMs(1000);
  dataRequestEvent_2 = new TimedEventGenerator(this, "onDataRequestEvent_2", false);
  dataRequestEvent_2.setIntervalMs(2000);
  blinkRequest = new TimedEventGenerator(this, "onBlinkRequest", false);
  blinkRequest.setIntervalMs(200);
  blinkAnswer = new TimedEventGenerator(this, "onBlinkAnswer", false);
  blinkAnswer.setIntervalMs(200);

  img_principal = loadImage("principal.png");
  img_eva = loadImage("eva.png");
  img_lavado = loadImage("lavado.png");
  img_aclarado = loadImage("aclarado.png");
  img_vaciado = loadImage("vaciado.png");
  img_presion = loadImage("presion.png");
  img_boya_inf = loadImage("boya inf.png");
  img_boya_sup = loadImage("boya sup.png");
  img_pres_bajo = loadImage("pres_bajo.png");
  img_puerta_abierta = loadImage("puerta abierta.png");
  img_puerta_cerrada = loadImage("puerta cerrada.png");
  img_c_cuba = loadImage("calentamiento cuba.png");
  img_c_calderin = loadImage("calentamiento calderin.png");
  img_botonOn = loadImage("botonOn.png");
  img_botonOff = loadImage("botonOff.png");
  img_logoSammic = loadImage("logo sammic.png");

  createGUI();
  customGUI();


  list_serial.setItems(serialList, 0);  
  printArray(serialList);
  offsetList.setItems(offSetListParameters, 0);
   
  //dataRequestEvent_1.setEnabled(false);  
  //dataRequestEvent_2.setEnabled(false);
}

void draw() {
  background(255);
  image(img_logoSammic,2,height-42,40,40);
  textSize(12);
  text("LavadoSNS  v0.2   250806", 55, height-8); 
  fill(0, 102, 153);

  image(img_principal, posx, posy, ix, iy);
  //stateEva = true;
  //stateBoya_sup = true;
  
  if (requestEvent_1) {
    onDataRequestParameters_1();
    requestEvent_1 = false;
  } else if (requestEvent_2) {
    onDataRequestParameters_2();
    requestEvent_2 = false;
  }

  if (requestDataRepresentation) {
    //printArray(splitDataAnswer);
    if (int(splitDataAnswer[0]) == IDspy) {
      link_answer_1(); 
      //println("espia");
    } else if (int(splitDataAnswer[0])==IDsensor) {
      link_answer_2(); 
      //println("sensor");
    }
    requestDataRepresentation = false;
  }

  if (blinkRequestLine) {
    strokeWeight(3); 
    stroke(250, 227, 152); //191, 255, 208
    line(350, 15, 395, 15);
  }
  if (blinkAnswerLine) {
    strokeWeight(3); 
    stroke(250, 227, 152); //191, 255, 208
    line(350, 35, 395, 35);
  }

  if (stateEva == true) { 
    image(img_eva, posx, posy, ix, iy);
  }
  if (stateB_lav == true) image(img_lavado, posx, posy, ix, iy);
  if (stateC_cald == true) {
    image(img_c_calderin, posx, posy, ix, iy);
    temperaturaAguaCalderin.setLocalColorScheme(GCScheme.YELLOW_SCHEME);
    temperaturaResistenciaCalderin.setLocalColorScheme(GCScheme.YELLOW_SCHEME);
  }
  else if (!stateC_cald == true) {
    temperaturaAguaCalderin.setLocalColorScheme(GCScheme.RED_SCHEME);
    temperaturaResistenciaCalderin.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  }
  if (stateC_cuba == true){
    image(img_c_cuba, posx, posy, ix, iy);
    temperaturaAguaCuba.setLocalColorScheme(GCScheme.YELLOW_SCHEME);
    temperaturaResistenciaCuba.setLocalColorScheme(GCScheme.YELLOW_SCHEME);
  }
  else if (!stateC_cuba == true){
    temperaturaAguaCuba.setLocalColorScheme(GCScheme.RED_SCHEME);
    temperaturaResistenciaCuba.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  }
  if (stateVaciado == true) image(img_vaciado, posx, posy, ix, iy);
  if (stateB_pres == true) image(img_aclarado, posx, posy, ix, iy);
  if (stateBoya_inf == true && stateBoya_sup == false ) image(img_boya_inf, posx, posy, ix, iy);
  if (stateBoya_sup == true) image(img_boya_sup, posx, posy, ix, iy);
  if (statePres_bajo == true) image(img_pres_bajo, posx, posy, ix, iy); 
  if (statePuerta == true) image(img_puerta_abierta, ix+posx, posy+10, ix*0.18, iy*0.80);
  if (!statePuerta == true) image(img_puerta_cerrada, ix+posx, posy+10, ix*0.18, iy*0.80);
  if (statePowerOn == true) image(img_botonOn, ix*0.96+posx, posy+25, ix*0.04, iy*0.07);
  if (!statePowerOn == true) image(img_botonOff, ix*0.96+posx, posy+25, ix*0.04, iy*0.07);
}

void serialConection() {
  if (!serialSelection) {
    myPort = new Serial(this, Serial.list()[list_serial.getSelectedIndex()], 115200);
    button_OKserial.setText("conectado");
    button_OKserial.setLocalColorScheme(GCScheme.GREEN_SCHEME);
    dataRequestEvent_1.setEnabled(true);
    dataRequestEvent_2.setEnabled(true);
    serialSelection = true;
  } else if (serialSelection) {
    myPort.clear();
    myPort.stop();
    button_OKserial.setText("conectar");
    button_OKserial.setLocalColorScheme(GCScheme.RED_SCHEME);
    dataRequestEvent_1.setEnabled(false);
    dataRequestEvent_2.setEnabled(false);
    serialSelection = false;
    blinkRequestLine= false;
  }
}

void link_answer_1() {
  for (int i=0; i<request1ArrayPos; i++) {
    if (requestDataArray_1[i] == "eva") {
      if (int(splitDataAnswer[i+1])==1) stateEva = true;
      else stateEva = false;
    } else if (requestDataArray_1[i] =="b_lav") {
      if (int(splitDataAnswer[i+1])==1) stateB_lav = true;
      else stateB_lav = false;
    } else if (requestDataArray_1[i] =="c_cald") {
      if (int(splitDataAnswer[i+1])==1) stateC_cald = true;
      else stateC_cald = false;
    } else if (requestDataArray_1[i] =="c_cuba") {
      if (int(splitDataAnswer[i+1])==1) stateC_cuba = true;
      else stateC_cuba = false;
    } else if (requestDataArray_1[i] =="vaciado") {
      if (int(splitDataAnswer[i+1])==1) stateVaciado = true;
      else stateVaciado = false;
    } else if (requestDataArray_1[i] =="b_pres") {
      if (int(splitDataAnswer[i+1])==1) stateB_pres = true;
      else stateB_pres = false;
    } else if (requestDataArray_1[i] =="powerOn") {
      if (int(splitDataAnswer[i+1])==1) statePowerOn = true;
      else statePowerOn = false;
    } else if (requestDataArray_1[i] =="puerta") {
      if (int(splitDataAnswer[i+1])==1) statePuerta = true;
      else statePuerta = false;
    } else if (requestDataArray_1[i] =="boya_sup") {
      if (int(splitDataAnswer[i+1])==1) stateBoya_sup = true;
      else stateBoya_sup = false;
    } else if (requestDataArray_1[i] =="boya_inf") {
      if (int(splitDataAnswer[i+1])==1) stateBoya_inf = true;
      else stateBoya_inf = false;
    } else if (requestDataArray_1[i] =="pres_bajo") {
      if (int(splitDataAnswer[i+1])==1) statePres_bajo = true;
      else statePres_bajo = false;
    } else if (requestDataArray_1[i] =="t_cuba") {
      stateT_cuba = float(splitDataAnswer[i+1])/10;
      temperaturaAguaCuba.setText("cuba "+str(stateT_cuba)+" ºC");
    } else if (requestDataArray_1[i] =="t_cald") {
      stateT_cald = float(splitDataAnswer[i+1])/10;
      temperaturaAguaCalderin.setText("calderin "+str(stateT_cald)+" ºC");
    } else if (requestDataArray_1[i] =="t_aclarado") {
      stateT_aclarado = float(splitDataAnswer[i+1])/10;
      temperaturaAguaAclarado.setText("aclarado "+str(stateT_aclarado)+" ºC");
    }
  }
}
void link_answer_2() {
  for (int i=0; i<request2ArrayPos; i++) {
    if (requestDataArray_2[i] =="spla") {// sensor presion lavado alta
      lavadoA.setText(str(float (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="splb") {// sensor preison labado baja
      lavadoB.setText(str(float (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="spaa") {// sensor presion aclarado alta
      aclaradoA.setText(str(float (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="spab") {// sensor presion aclarado baja
      aclaradoB.setText(str(float (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="spd") {// sensor presion dosificador
      dosificador.setText(str(float (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="scab") {// sensor caudal aclarado baja
      aclaradoBcaudal.setText(str(float (splitDataAnswer[i+1])/10) + " l/min");
    } else if (requestDataArray_2[i] =="stca") {// sensor temp. resistencia calderin
      float tResistenciaCalderin = float (splitDataAnswer[i+1])/10;
      temperaturaResistenciaCalderin.setText("Resistencia "+ str(tResistenciaCalderin) + " ºC");
    } else if (requestDataArray_2[i] =="stcu") {// sensor temp. resistencia cuba
      float tResistenciaCuba = float(splitDataAnswer[i+1])/10;
      temperaturaResistenciaCuba.setText("Resistencia "+ str(tResistenciaCuba) + " ºC");
    } else if (requestDataArray_2[i] =="st") {// sensor turbidez
      turbidez.setText(str(float(splitDataAnswer[i+1])/100)+" NTU");
    }
  }
}

void check_boxes_1() {
  request1ArrayPos = 0;
  if (eva.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "eva"; 
    request1ArrayPos++;
  }
  if (b_lav.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "b_lav"; 
    request1ArrayPos++;
  }
  if (c_calderin.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "c_cald"; 
    request1ArrayPos++;
  }
  if (c_cuba.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "c_cuba"; 
    request1ArrayPos++;
  }
  if (vaciado.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "vaciado"; 
    request1ArrayPos++;
  }
  if (b_presion.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "b_pres"; 
    request1ArrayPos++;
  }
  if (powerOn.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "powerOn"; 
    request1ArrayPos++;
  }
  if (puerta.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "puerta"; 
    request1ArrayPos++;
  }
  if (boya_inf.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "boya_inf"; 
    request1ArrayPos++;
  }
  if (boya_sup.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "boya_sup"; 
    request1ArrayPos++;
  }
  if (pres_bajo.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "pres_bajo"; 
    request1ArrayPos++;
  }
  if (temp_cuba.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "t_cuba"; 
    request1ArrayPos++;
  }
  if (temp_calderin.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "t_cald"; 
    request1ArrayPos++;
  }
  if (temp_aclarado.isSelected()) {
    requestDataArray_1[request1ArrayPos]= "t_aclarado"; 
    request1ArrayPos++;
  }
}

void check_boxes_2() {
  request2ArrayPos = 0;
  if (presionLavadoA.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "spla"; 
    request2ArrayPos++;
  }
  if (presionAclaradoA.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "spaa"; 
    request2ArrayPos++;
  }
  if (presionDosificador.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "spd"; 
    request2ArrayPos++;
  }
  if (presionLavadoB.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "splb"; 
    request2ArrayPos++;
  }
  if (presionAclaradoB.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "spab"; 
    request2ArrayPos++;
  }
  if (caudalAclaradoB.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "scab"; 
    request2ArrayPos++;
  }
  if (turbidezCuba.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "st"; 
    request2ArrayPos++;
  }
  if (tResistenciaCuba.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "stcu"; 
    request2ArrayPos++;
  }
  if (tResistenciaCalderin.isSelected()) {
    requestDataArray_2[request2ArrayPos]= "stca"; 
    request2ArrayPos++;
  }
}

void onDataRequestEvent_1() {
  requestEvent_1 = true;
}

void onDataRequestParameters_1() {
  String requestString_1="";
  String[] subsetString_1; //<>//
  check_boxes_1();
  IDspy = int(random(1, 2500)*random(1, 2500));
  if (request1ArrayPos==0) {
    requestString_1="%"+ str(IDspy) +"$";
  } 
  else if(request1ArrayPos>0){
    // extraer los parametros de peticion nuevos
    subsetString_1 = subset(requestDataArray_1, 0, request1ArrayPos);
    // convertir en un único String uniendo mediante una coma
    requestString_1 = join(subsetString_1, ",");
    // dar formato a la peticion de datos
    requestString_1="%"+ str(IDspy)+","+requestString_1 +"$";
  }
  //else requestString_1 ="";
  // mostrar en la pantalla
  request.setText(requestString_1);
  // enviar por el puerto serie
  myPort.write(requestString_1);
  //parpadeo luminosa
  blinkRequestLine= true;
  blinkRequest.setEnabled(true);
}

void onDataRequestEvent_2() {
  requestEvent_2 = true;
}

void onDataRequestParameters_2() {
  String requestString_2="";
  String[] subsetString_2;
  check_boxes_2();
  IDsensor = int(random(1, 2500)*random(1, 2500));
  if (request1ArrayPos==0) {
    requestString_2="%"+ str(IDsensor) +"$";
  } else {
    // extraer los parametros de peticion nuevos
    subsetString_2 = subset(requestDataArray_2, 0, request2ArrayPos);
    // convertir en un único String uniendo mediante una coma
    requestString_2 = join(subsetString_2, ",");
    // dar formato a la peticion de datos
    requestString_2="%"+ str(IDsensor)+","+requestString_2 +"$";
  }
  // mostrar en la pantalla
  request.setText(requestString_2);
  // enviar por el puerto serie
  myPort.write(requestString_2);
  //parpadeo luminosa
  blinkRequestLine= true;
  blinkRequest.setEnabled(true);
}

void onBlinkRequest() {
  if (blinkRequestLine==false) {
    blinkRequest.setEnabled(false);
  }
  else if (blinkRequestLine==true) {
     blinkRequestLine = !blinkRequestLine;
  }
}

void onBlinkAnswer() {
  if (blinkAnswerLine==false) {
    blinkAnswer.setEnabled(false);
  }
  else if(blinkAnswerLine==true){
    blinkAnswerLine = !blinkAnswerLine;
  }
}

void cambiarUnidad(){
  if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia calderin") {
    unidad.setText("ºC");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia cuba") {
    unidad.setText("ºC");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion lavado A") {
    unidad.setText("bar");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion lavado B") {
    unidad.setText("bar");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion aclarado A") {
    unidad.setText("bar");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion aclarado B") {
    unidad.setText("bar");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion dosificador") {
    unidad.setText("bar");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="caudal aclarado B") {
    unidad.setText("l/min");
  }
}

void sendOffset() {
  //print(offSetListParameters[offsetList.getSelectedIndex()]);
  String valorString = valorOffset.getText();
  String parameter="";
  float valorNum = 0;
  String offsetSerial;
  if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia calderin") {
    IDoffset = 2;
    parameter = "os_stca";
    valorNum = float(valorString)*10;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia cuba") {
    IDoffset = 1;
    parameter = "os_stcu";
    valorNum = float(valorString)*10;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion lavado A") {
    IDoffset = 3;
    parameter = "os_spla";
    valorNum = float(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion lavado B") {
    IDoffset = 4;
    parameter = "os_splb";
    valorNum = float(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion aclarado A") {
    IDoffset = 5;
    parameter = "os_spaa";
    valorNum = float(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion aclarado B") {
    IDoffset = 6;
    parameter = "os_spab";
    valorNum = float(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion dosificador") {
    IDoffset = 7;
    parameter = "os_spd";
    valorNum = float(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="caudal aclarado B") {
    IDoffset = 8;
    parameter = "os_scab";
    valorNum = float(valorString)*10;
  }
  offsetSerial = "%"+str(IDoffset)+","+ parameter+ "," +str(int(valorNum))+"$";
  if (serialSelection==true) {
    myPort.write(offsetSerial);
    request.setText(offsetSerial);
  }
  valorOffset.setText("");
  print(offsetSerial);
}

void serialEvent(Serial p) { 
  char inByte = p.readChar();
  //print(inByte);
  if (inByte == '%') {
    triggerAppend = true;
  } else if (inByte== '$') {
    //println(inString);
    answer.setText("%"+inString+"$");
    blinkAnswer.setEnabled(true);
    splitDataAnswer = split(inString, ','); // separar los variables por coma en el array splitDat
    inString ="";
    requestDataRepresentation = true;
    triggerAppend = false;
  }
  if (triggerAppend==true && inByte!='%') {  
    inString = inString + str(inByte);
  }
}  

void mouseClicked() {
  //list_serial.setItems(serialList, 0);
}