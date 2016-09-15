import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import g4p_controls.*; 
import org.multiply.processing.TimedEventGenerator; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class lavado_sense extends PApplet {

//%1,eva,b_lav,c_cald,c_cuba,vaciado,b_pres,powerOn,puerta,boya_inf,boya_sup,pres_bajo,t_cuba,t_cald,t_aclarado$
//%2,spla,splb,spaa,spab,spd,scab,stca,stcu,st$
// 5 de agosto del 2016




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


public void setup() {
  frameRate(15);
  
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

public void draw() {
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
    if (PApplet.parseInt(splitDataAnswer[0]) == IDspy) {
      link_answer_1(); 
      //println("espia");
    } else if (PApplet.parseInt(splitDataAnswer[0])==IDsensor) {
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
  if (statePuerta == true) image(img_puerta_abierta, ix+posx, posy+10, ix*0.18f, iy*0.80f);
  if (!statePuerta == true) image(img_puerta_cerrada, ix+posx, posy+10, ix*0.18f, iy*0.80f);
  if (statePowerOn == true) image(img_botonOn, ix*0.96f+posx, posy+25, ix*0.04f, iy*0.07f);
  if (!statePowerOn == true) image(img_botonOff, ix*0.96f+posx, posy+25, ix*0.04f, iy*0.07f);
}

public void serialConection() {
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

public void link_answer_1() {
  for (int i=0; i<request1ArrayPos; i++) {
    if (requestDataArray_1[i] == "eva") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateEva = true;
      else stateEva = false;
    } else if (requestDataArray_1[i] =="b_lav") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateB_lav = true;
      else stateB_lav = false;
    } else if (requestDataArray_1[i] =="c_cald") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateC_cald = true;
      else stateC_cald = false;
    } else if (requestDataArray_1[i] =="c_cuba") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateC_cuba = true;
      else stateC_cuba = false;
    } else if (requestDataArray_1[i] =="vaciado") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateVaciado = true;
      else stateVaciado = false;
    } else if (requestDataArray_1[i] =="b_pres") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateB_pres = true;
      else stateB_pres = false;
    } else if (requestDataArray_1[i] =="powerOn") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) statePowerOn = true;
      else statePowerOn = false;
    } else if (requestDataArray_1[i] =="puerta") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) statePuerta = true;
      else statePuerta = false;
    } else if (requestDataArray_1[i] =="boya_sup") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateBoya_sup = true;
      else stateBoya_sup = false;
    } else if (requestDataArray_1[i] =="boya_inf") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) stateBoya_inf = true;
      else stateBoya_inf = false;
    } else if (requestDataArray_1[i] =="pres_bajo") {
      if (PApplet.parseInt(splitDataAnswer[i+1])==1) statePres_bajo = true;
      else statePres_bajo = false;
    } else if (requestDataArray_1[i] =="t_cuba") {
      stateT_cuba = PApplet.parseFloat(splitDataAnswer[i+1])/10;
      temperaturaAguaCuba.setText("cuba "+str(stateT_cuba)+" \u00baC");
    } else if (requestDataArray_1[i] =="t_cald") {
      stateT_cald = PApplet.parseFloat(splitDataAnswer[i+1])/10;
      temperaturaAguaCalderin.setText("calderin "+str(stateT_cald)+" \u00baC");
    } else if (requestDataArray_1[i] =="t_aclarado") {
      stateT_aclarado = PApplet.parseFloat(splitDataAnswer[i+1])/10;
      temperaturaAguaAclarado.setText("aclarado "+str(stateT_aclarado)+" \u00baC");
    }
  }
}
public void link_answer_2() {
  for (int i=0; i<request2ArrayPos; i++) {
    if (requestDataArray_2[i] =="spla") {// sensor presion lavado alta
      lavadoA.setText(str(PApplet.parseFloat (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="splb") {// sensor preison labado baja
      lavadoB.setText(str(PApplet.parseFloat (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="spaa") {// sensor presion aclarado alta
      aclaradoA.setText(str(PApplet.parseFloat (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="spab") {// sensor presion aclarado baja
      aclaradoB.setText(str(PApplet.parseFloat (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="spd") {// sensor presion dosificador
      dosificador.setText(str(PApplet.parseFloat (splitDataAnswer[i+1])/100) + " bar");
    } else if (requestDataArray_2[i] =="scab") {// sensor caudal aclarado baja
      aclaradoBcaudal.setText(str(PApplet.parseFloat (splitDataAnswer[i+1])/10) + " l/min");
    } else if (requestDataArray_2[i] =="stca") {// sensor temp. resistencia calderin
      float tResistenciaCalderin = PApplet.parseFloat (splitDataAnswer[i+1])/10;
      temperaturaResistenciaCalderin.setText("Resistencia "+ str(tResistenciaCalderin) + " \u00baC");
    } else if (requestDataArray_2[i] =="stcu") {// sensor temp. resistencia cuba
      float tResistenciaCuba = PApplet.parseFloat(splitDataAnswer[i+1])/10;
      temperaturaResistenciaCuba.setText("Resistencia "+ str(tResistenciaCuba) + " \u00baC");
    } else if (requestDataArray_2[i] =="st") {// sensor turbidez
      turbidez.setText(str(PApplet.parseFloat(splitDataAnswer[i+1])/100)+" NTU");
    }
  }
}

public void check_boxes_1() {
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

public void check_boxes_2() {
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

public void onDataRequestEvent_1() {
  requestEvent_1 = true;
}

public void onDataRequestParameters_1() {
  String requestString_1="";
  String[] subsetString_1; //<>//
  check_boxes_1();
  IDspy = PApplet.parseInt(random(1, 2500)*random(1, 2500));
  if (request1ArrayPos==0) {
    requestString_1="%"+ str(IDspy) +"$";
  } 
  else if(request1ArrayPos>0){
    // extraer los parametros de peticion nuevos
    subsetString_1 = subset(requestDataArray_1, 0, request1ArrayPos);
    // convertir en un \u00fanico String uniendo mediante una coma
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

public void onDataRequestEvent_2() {
  requestEvent_2 = true;
}

public void onDataRequestParameters_2() {
  String requestString_2="";
  String[] subsetString_2;
  check_boxes_2();
  IDsensor = PApplet.parseInt(random(1, 2500)*random(1, 2500));
  if (request1ArrayPos==0) {
    requestString_2="%"+ str(IDsensor) +"$";
  } else {
    // extraer los parametros de peticion nuevos
    subsetString_2 = subset(requestDataArray_2, 0, request2ArrayPos);
    // convertir en un \u00fanico String uniendo mediante una coma
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

public void onBlinkRequest() {
  if (blinkRequestLine==false) {
    blinkRequest.setEnabled(false);
  }
  else if (blinkRequestLine==true) {
     blinkRequestLine = !blinkRequestLine;
  }
}

public void onBlinkAnswer() {
  if (blinkAnswerLine==false) {
    blinkAnswer.setEnabled(false);
  }
  else if(blinkAnswerLine==true){
    blinkAnswerLine = !blinkAnswerLine;
  }
}

public void cambiarUnidad(){
  if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia calderin") {
    unidad.setText("\u00baC");
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia cuba") {
    unidad.setText("\u00baC");
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

public void sendOffset() {
  //print(offSetListParameters[offsetList.getSelectedIndex()]);
  String valorString = valorOffset.getText();
  String parameter="";
  float valorNum = 0;
  String offsetSerial;
  if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia calderin") {
    IDoffset = 2;
    parameter = "os_stca";
    valorNum = PApplet.parseFloat(valorString)*10;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="temperatura resistencia cuba") {
    IDoffset = 1;
    parameter = "os_stcu";
    valorNum = PApplet.parseFloat(valorString)*10;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion lavado A") {
    IDoffset = 3;
    parameter = "os_spla";
    valorNum = PApplet.parseFloat(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion lavado B") {
    IDoffset = 4;
    parameter = "os_splb";
    valorNum = PApplet.parseFloat(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion aclarado A") {
    IDoffset = 5;
    parameter = "os_spaa";
    valorNum = PApplet.parseFloat(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion aclarado B") {
    IDoffset = 6;
    parameter = "os_spab";
    valorNum = PApplet.parseFloat(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="presion dosificador") {
    IDoffset = 7;
    parameter = "os_spd";
    valorNum = PApplet.parseFloat(valorString)*100;
  } else if (offSetListParameters[offsetList.getSelectedIndex()]=="caudal aclarado B") {
    IDoffset = 8;
    parameter = "os_scab";
    valorNum = PApplet.parseFloat(valorString)*10;
  }
  offsetSerial = "%"+str(IDoffset)+","+ parameter+ "," +str(PApplet.parseInt(valorNum))+"$";
  if (serialSelection==true) {
    myPort.write(offsetSerial);
    request.setText(offsetSerial);
  }
  valorOffset.setText("");
  print(offsetSerial);
}

public void serialEvent(Serial p) { 
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

public void mouseClicked() {
  //list_serial.setItems(serialList, 0);
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

public void list_serial_click(GDropList source, GEvent event) { //_CODE_:list_serial:790909:
  println("dropList1 - GDropList >> GEvent." + event + " @ " + millis());
} //_CODE_:list_serial:790909:


public void button_OKserial_click(GButton source, GEvent event) { //_CODE_:button_OKserial:355725:
  println("button1 - GButton >> GEvent." + event + " @ " + millis());
  serialConection();
} //_CODE_:button_OKserial:355725:

public void eva_clicked1(GCheckbox source, GEvent event) { //_CODE_:eva:841145:
  println("checkbox1 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:eva:841145:

public void b_lav_clicked1(GCheckbox source, GEvent event) { //_CODE_:b_lav:594751:
  println("checkbox2 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:b_lav:594751:

public void c_calderin_clicked1(GCheckbox source, GEvent event) { //_CODE_:c_calderin:901309:
  println("checkbox3 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:c_calderin:901309:

public void c_cuba_clicked1(GCheckbox source, GEvent event) { //_CODE_:c_cuba:437297:
  println("checkbox4 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:c_cuba:437297:

public void vaciado_clicked1(GCheckbox source, GEvent event) { //_CODE_:vaciado:536116:
  println("checkbox1 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:vaciado:536116:

public void b_presion_clicked1(GCheckbox source, GEvent event) { //_CODE_:b_presion:303377:
  println("b_presion - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:b_presion:303377:

public void powerOn_clicked1(GCheckbox source, GEvent event) { //_CODE_:powerOn:604669:
  println("checkbox1 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:powerOn:604669:

public void puerta_clicked1(GCheckbox source, GEvent event) { //_CODE_:puerta:417767:
  println("puerta - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:puerta:417767:

public void boya_inf_clicked1(GCheckbox source, GEvent event) { //_CODE_:boya_inf:748712:
  println("checkbox1 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:boya_inf:748712:

public void boya_sup_clicked1(GCheckbox source, GEvent event) { //_CODE_:boya_sup:838148:
  println("checkbox2 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:boya_sup:838148:

public void pres_bajo_clicked1(GCheckbox source, GEvent event) { //_CODE_:pres_bajo:308185:
  println("checkbox3 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:pres_bajo:308185:

public void temp_cuba_clicked1(GCheckbox source, GEvent event) { //_CODE_:temp_cuba:680224:
  println("checkbox1 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:temp_cuba:680224:

public void temp_calderin_clicked1(GCheckbox source, GEvent event) { //_CODE_:temp_calderin:716182:
  println("checkbox2 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:temp_calderin:716182:

public void temp_aclarado_clicked1(GCheckbox source, GEvent event) { //_CODE_:temp_aclarado:834010:
  println("temp_aclarado - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:temp_aclarado:834010:

public void presionLavadoA_clicked1(GCheckbox source, GEvent event) { //_CODE_:presionLavadoA:226443:
  println("checkbox1 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:presionLavadoA:226443:

public void presionAclaradoA_clicked1(GCheckbox source, GEvent event) { //_CODE_:presionAclaradoA:282275:
  println("checkbox2 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:presionAclaradoA:282275:

public void presionDosificador_clicked1(GCheckbox source, GEvent event) { //_CODE_:presionDosificador:517929:
  println("checkbox3 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:presionDosificador:517929:

public void presionLavadoB_clicked1(GCheckbox source, GEvent event) { //_CODE_:presionLavadoB:931233:
  println("checkbox4 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:presionLavadoB:931233:

public void presionAclaradoB_clicked1(GCheckbox source, GEvent event) { //_CODE_:presionAclaradoB:831011:
  println("presionAclaradoB - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:presionAclaradoB:831011:

public void caudalAclaradoB_clicked1(GCheckbox source, GEvent event) { //_CODE_:caudalAclaradoB:751230:
  println("caudalAclaradoB - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:caudalAclaradoB:751230:

public void turbidezCuba_clicked1(GCheckbox source, GEvent event) { //_CODE_:turbidezCuba:438314:
  println("checkbox1 - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:turbidezCuba:438314:

public void tResistenciaCuba_clicked1(GCheckbox source, GEvent event) { //_CODE_:tResistenciaCuba:846279:
  println("tResistenciaCuba - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:tResistenciaCuba:846279:

public void tResistenciaCalderin_clicked1(GCheckbox source, GEvent event) { //_CODE_:tResistenciaCalderin:762939:
  println("tResistenciaCalderin - GCheckbox >> GEvent." + event + " @ " + millis());
} //_CODE_:tResistenciaCalderin:762939:

public void notas_change1(GTextArea source, GEvent event) { //_CODE_:notas:526701:
  println("notas - GTextArea >> GEvent." + event + " @ " + millis());
} //_CODE_:notas:526701:

public void offSetList_click1(GDropList source, GEvent event) { //_CODE_:offsetList:435603:
  println("dropList1 - GDropList >> GEvent." + event + " @ " + millis());
    cambiarUnidad();
} //_CODE_:offsetList:435603:

public void valorOffset_change1(GTextField source, GEvent event) { //_CODE_:valorOffset:336321:
  println("textfield1 - GTextField >> GEvent." + event + " @ " + millis());

} //_CODE_:valorOffset:336321:

public void enviarOffset_click1(GButton source, GEvent event) { //_CODE_:enviarOffset:575314:
  println("enviarOffset - GButton >> GEvent." + event + " @ " + millis());
  sendOffset();
} //_CODE_:enviarOffset:575314:


// Create all the GUI controls. 
// autogenerated do not edit
public void customGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  surface.setTitle("Sketch Window");
  
  list_serial = new GDropList(this, 14, 8, 252, 100, 5);
  //list_serial.setItems(loadStrings("list_790909"), 0);
  list_serial.addEventHandler(this, "list_serial_click");
  button_OKserial = new GButton(this, 270, 8, 65, 20);
  button_OKserial.setText("conectar");
  button_OKserial.setLocalColorScheme(GCScheme.RED_SCHEME);
  button_OKserial.addEventHandler(this, "button_OKserial_click");
  
  eva = new GCheckbox(this, 5, 30, 45, 19);
  eva.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  eva.setText("eva");
  eva.setOpaque(false);
  eva.addEventHandler(this, "eva_clicked1");
  eva.setSelected(true);
  b_lav = new GCheckbox(this, 5, 50, 58, 20);
  b_lav.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  b_lav.setText("b_lav");
  b_lav.setOpaque(false);
  b_lav.addEventHandler(this, "b_lav_clicked1");
  b_lav.setSelected(true);
  c_calderin = new GCheckbox(this, 85, 90, 81, 20);
  c_calderin.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  c_calderin.setText("c_calderin");
  c_calderin.setOpaque(false);
  c_calderin.addEventHandler(this, "c_calderin_clicked1");
  c_calderin.setSelected(true);
  c_cuba = new GCheckbox(this, 5, 90, 80, 20); 
  c_cuba.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  c_cuba.setText("c_cuba");
  c_cuba.setOpaque(false);
  c_cuba.addEventHandler(this, "c_cuba_clicked1");
  c_cuba.setSelected(true);
  vaciado = new GCheckbox(this, 5, 70, 68, 21);      
  vaciado.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  vaciado.setText("vaciado");
  vaciado.setOpaque(false);
  vaciado.addEventHandler(this, "vaciado_clicked1");
  vaciado.setSelected(true);
  b_presion = new GCheckbox(this, 85, 50, 80, 18);  
  b_presion.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  b_presion.setText("b_presion");
  b_presion.setOpaque(false);
  b_presion.addEventHandler(this, "b_presion_clicked1");
  b_presion.setSelected(true);
  powerOn = new GCheckbox(this, 85, 30, 78, 20);
  powerOn.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  powerOn.setText("powerOn");
  powerOn.setOpaque(false);
  powerOn.addEventHandler(this, "powerOn_clicked1");
  powerOn.setSelected(true);
  puerta = new GCheckbox(this, 85, 70, 80, 20);    
  puerta.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  puerta.setText("puerta");
  puerta.setOpaque(false);
  puerta.addEventHandler(this, "puerta_clicked1");
  puerta.setSelected(true);
  boya_inf = new GCheckbox(this, 85, 170, 120, 20);
  boya_inf.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  boya_inf.setText("boya_inf");
  boya_inf.setOpaque(false);
  boya_inf.addEventHandler(this, "boya_inf_clicked1");
  boya_inf.setSelected(true);
  boya_sup = new GCheckbox(this, 5, 170, 120, 20);
  boya_sup.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  boya_sup.setText("boya_sup");
  boya_sup.setOpaque(false);
  boya_sup.addEventHandler(this, "boya_sup_clicked1");
  boya_sup.setSelected(true);
  pres_bajo = new GCheckbox(this, 5, 150, 120, 20);
  pres_bajo.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  pres_bajo.setText("pres_bajo");
  pres_bajo.setOpaque(false);
  pres_bajo.addEventHandler(this, "pres_bajo_clicked1");
  pres_bajo.setSelected(true);
  temp_cuba = new GCheckbox(this, 5, 110, 93, 20);
  temp_cuba.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temp_cuba.setText("cuba \u00baC");
  temp_cuba.setOpaque(false);
  temp_cuba.addEventHandler(this, "temp_cuba_clicked1");
  temp_cuba.setSelected(true);
  temp_calderin = new GCheckbox(this, 85, 110, 82, 20);
  temp_calderin.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temp_calderin.setText("calderin \u00baC");
  temp_calderin.setOpaque(false);
  temp_calderin.addEventHandler(this, "temp_calderin_clicked1");
  temp_calderin.setSelected(true);
  temp_aclarado = new GCheckbox(this, 5, 130, 120, 20);
  temp_aclarado.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temp_aclarado.setText("aclarado \u00baC");
  temp_aclarado.setOpaque(false);
  temp_aclarado.addEventHandler(this, "temp_aclarado_clicked1");
  temp_aclarado.setSelected(true);
  
  label1 = new GLabel(this, 340, 1, 65, 20);
  label1.setText("petici\u00f3n:");
  //label1.setTextBold();
  label1.setLocalColorScheme(GCScheme.GOLD_SCHEME);
  label1.setOpaque(false);
  label2 = new GLabel(this, 342, 17, 65, 20);
  label2.setText("respuesta:");
  //label2.setTextBold();
  label2.setLocalColorScheme(GCScheme.GOLD_SCHEME);
  label2.setOpaque(false);
  request = new GLabel(this, 405, 1, 700, 20);
  request.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  request.setText("...");
  request.setLocalColorScheme(GCScheme.GOLD_SCHEME);
  request.setOpaque(false);
  answer = new GLabel(this, 405, 17, 700, 20);
  answer.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  answer.setText("...");
  answer.setLocalColorScheme(GCScheme.GOLD_SCHEME);
  answer.setOpaque(false);
  
  presionLavadoA = new GCheckbox(this, 5, 220, 120, 20);
  presionLavadoA.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //presionLavadoA.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  presionLavadoA.setText("presion lavado A");
  presionLavadoA.setOpaque(false);
  presionLavadoA.addEventHandler(this, "presionLavadoA_clicked1");
  presionLavadoA.setSelected(true);
  presionAclaradoA = new GCheckbox(this, 5, 240, 130, 20);
  presionAclaradoA.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
 // presionAclaradoA.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  presionAclaradoA.setText("presion aclarado A");
  presionAclaradoA.setOpaque(false);
  presionAclaradoA.addEventHandler(this, "presionAclaradoA_clicked1");
  presionAclaradoA.setSelected(true);
  presionDosificador = new GCheckbox(this, 5, 260, 130, 20);
  presionDosificador.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //presionDosificador.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  presionDosificador.setText("presion dosificador");
  presionDosificador.setOpaque(false);
  presionDosificador.addEventHandler(this, "presionDosificador_clicked1");
  presionDosificador.setSelected(true);
  presionLavadoB = new GCheckbox(this, 130, 220, 120, 20);
  presionLavadoB.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //presionLavadoB.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  presionLavadoB.setText("presion lavado B");
  presionLavadoB.setOpaque(false);
  presionLavadoB.addEventHandler(this, "presionLavadoB_clicked1");
  presionLavadoB.setSelected(true);
  presionAclaradoB = new GCheckbox(this, 130, 240, 130, 20);
  presionAclaradoB.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //presionAclaradoB.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  presionAclaradoB.setText("presion aclarado B");
  presionAclaradoB.setOpaque(false);
  presionAclaradoB.addEventHandler(this, "presionAclaradoB_clicked1");
  presionAclaradoB.setSelected(true);
  caudalAclaradoB = new GCheckbox(this, 130, 280, 120, 20);
  caudalAclaradoB.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //caudalAclaradoB.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  caudalAclaradoB.setText("caudal aclarado B");
  caudalAclaradoB.setOpaque(false);
  caudalAclaradoB.addEventHandler(this, "caudalAclaradoB_clicked1");
  caudalAclaradoB.setSelected(true);
  turbidezCuba = new GCheckbox(this, 5, 300, 120, 20);
  turbidezCuba.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //turbidezCuba.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  turbidezCuba.setText("turbidez cuba");
  turbidezCuba.setOpaque(false);
  turbidezCuba.addEventHandler(this, "turbidezCuba_clicked1");
  turbidezCuba.setSelected(true);
  tResistenciaCuba = new GCheckbox(this, 5, 320, 140, 20);
  tResistenciaCuba.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //tResistenciaCuba.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  tResistenciaCuba.setText("resistencia cuba \u00baC");
  tResistenciaCuba.setOpaque(false);
  tResistenciaCuba.addEventHandler(this, "tResistenciaCuba_clicked1");
  tResistenciaCuba.setSelected(true);
  tResistenciaCalderin = new GCheckbox(this, 130, 320, 150, 20);
  tResistenciaCalderin.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  //tResistenciaCalderin.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  tResistenciaCalderin.setText("resistencia calderin \u00baC");
  tResistenciaCalderin.setOpaque(false);
  tResistenciaCalderin.addEventHandler(this, "tResistenciaCalderin_clicked1");
  tResistenciaCalderin.setSelected(true);
  
  temperaturaAguaCuba = new GLabel(this, 594, 300, 90, 20);
  temperaturaAguaCuba.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temperaturaAguaCuba.setText("agua cuba \u00baC");
  temperaturaAguaCuba.setLocalColorScheme(GCScheme.RED_SCHEME);
  temperaturaAguaCuba.setOpaque(false);
  temperaturaAguaCalderin = new GLabel(this, 365, 220, 100, 20);
  temperaturaAguaCalderin.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temperaturaAguaCalderin.setText("agua calderin \u00baC");
  temperaturaAguaCalderin.setLocalColorScheme(GCScheme.RED_SCHEME);
  temperaturaAguaCalderin.setOpaque(false);
  temperaturaAguaAclarado = new GLabel(this, 550, 45, 100, 20);
  temperaturaAguaAclarado.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temperaturaAguaAclarado.setText("agua aclarado \u00baC");
  temperaturaAguaAclarado.setLocalColorScheme(GCScheme.RED_SCHEME);
  temperaturaAguaAclarado.setOpaque(false);
  
  temperaturaResistenciaCuba = new GLabel(this, 580, 325, 120, 20);
  temperaturaResistenciaCuba.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temperaturaResistenciaCuba.setText("resistencia cuba \u00baC");
  temperaturaResistenciaCuba.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  temperaturaResistenciaCuba.setOpaque(false);
  temperaturaResistenciaCalderin = new GLabel(this, 350, 245, 125, 20);
  temperaturaResistenciaCalderin.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  temperaturaResistenciaCalderin.setText("resistencia calderin \u00baC");
  temperaturaResistenciaCalderin.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  temperaturaResistenciaCalderin.setOpaque(false);
  
  lavadoA = new GLabel(this, 670, 70, 80, 20);
  lavadoA.setText("lavado bar");
  lavadoA.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lavadoA.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  lavadoA.setOpaque(false);
  aclaradoA = new GLabel(this, 572, 70, 80, 20);
  aclaradoA.setText("aclarado bar");
  aclaradoA.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  aclaradoA.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  aclaradoA.setOpaque(false);
  aclaradoB = new GLabel(this, 572, 220, 80, 20);
  aclaradoB.setText("aclarado bar");
  aclaradoB.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  aclaradoB.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  aclaradoB.setOpaque(false);
  lavadoB = new GLabel(this, 670, 220, 80, 20);
  lavadoB.setText("lavado bar");
  lavadoB.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lavadoB.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  lavadoB.setOpaque(false);
  turbidez = new GLabel(this, 600, 350, 80, 20);
  turbidez.setText("turbidez ntu");
  turbidez.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  turbidez.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  turbidez.setOpaque(false);
  dosificador = new GLabel(this, 775, 350, 100, 20);
  dosificador.setText("dosificador bar");
  dosificador.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  dosificador.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  dosificador.setOpaque(false);
  aclaradoBcaudal = new GLabel(this, 572, 245, 80, 20);
  aclaradoBcaudal.setText("aclarado l/min");
  aclaradoBcaudal.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  aclaradoBcaudal.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
  aclaradoBcaudal.setOpaque(false);
  
  notas = new GTextArea(this, 750, 380, 233, 118, G4P.SCROLLBARS_NONE);
  notas.setPromptText("espacio para notas...");
  notas.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  notas.setOpaque(true);
  notas.addEventHandler(this, "notas_change1");
  offsetList = new GDropList(this, 290, 395, 223, 105, 4);
  //offsetList.setItems(loadStrings("list_435603"), 0);
  offsetList.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  offsetList.addEventHandler(this, "offSetList_click1");
  offset = new GLabel(this, 245, 395, 45, 20);
  offset.setText("offset :");
  offset.setTextBold();
  offset.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  offset.setOpaque(false);
  valorOffset = new GTextField(this, 558, 395, 34, 20, G4P.SCROLLBARS_NONE);
  valorOffset.setPromptText("........");
  valorOffset.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  valorOffset.setOpaque(true);
  valorOffset.addEventHandler(this, "valorOffset_change1");
  labelValorOffset = new GLabel(this, 515, 395, 45, 20);
  labelValorOffset.setText("valor :");
  labelValorOffset.setTextBold();
  labelValorOffset.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  labelValorOffset.setOpaque(false);
  enviarOffset = new GButton(this, 635, 395, 55, 20);
  enviarOffset.setText("cambiar");
  enviarOffset.setTextBold();
  enviarOffset.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  enviarOffset.addEventHandler(this, "enviarOffset_click1");
  unidad = new GLabel(this, 593, 395, 40, 20);
  unidad.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  unidad.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  unidad.setText("\u00baC");
  unidad.setOpaque(false);
}

// Variable declarations 
// autogenerated do not edit
GDropList list_serial; 
GButton button_OKserial; 

GCheckbox eva; 
GCheckbox b_lav; 
GCheckbox c_calderin; 
GCheckbox c_cuba; 
GCheckbox vaciado; 
GCheckbox b_presion; 
GCheckbox powerOn; 
GCheckbox puerta; 
GCheckbox boya_inf; 
GCheckbox boya_sup; 
GCheckbox pres_bajo; 
GCheckbox temp_cuba; 
GCheckbox temp_calderin; 
GCheckbox temp_aclarado; 

GLabel label1; 
GLabel label2; 
GLabel request; 
GLabel answer; 

GCheckbox presionLavadoA; 
GCheckbox presionAclaradoA; 
GCheckbox presionDosificador; 
GCheckbox presionLavadoB; 
GCheckbox presionAclaradoB; 
GCheckbox caudalAclaradoB; 
GCheckbox turbidezCuba; 
GCheckbox tResistenciaCuba; 
GCheckbox tResistenciaCalderin; 

GLabel temperaturaAguaCuba; 
GLabel temperaturaAguaCalderin; 
GLabel temperaturaAguaAclarado; 

GLabel temperaturaResistenciaCuba; 
GLabel temperaturaResistenciaCalderin; 

GLabel lavadoA; 
GLabel aclaradoA; 
GLabel aclaradoB; 
GLabel lavadoB; 
GLabel turbidez; 
GLabel dosificador; 
GLabel aclaradoBcaudal; 

GTextArea notas; 
GDropList offsetList; 
GLabel offset; 
GTextField valorOffset; 
GLabel labelValorOffset; 
GButton enviarOffset; 
GLabel unidad; 
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  surface.setTitle("Sketch Window");
}

// Variable declarations 
// autogenerated do not edit
  public void settings() {  size(1050, 500); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "lavado_sense" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
