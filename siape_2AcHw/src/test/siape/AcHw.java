/*
 *  Andre Cavalcante, Rafael Mendonca, Hiram Amaral
 *  Copyright UFAM 2015-2016
 */
package test.siape;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import eps.Debug;
import eps.MRA;
import eps.MRAInfo;
import eps.Skill;
import eps.SkillExecuteException;
import eps.Util;

/**
 * Realiza o acesso ao hardware do RaspberryPi
 *
 * @author hiramaral
 */
public class AcHw extends MRA {

    final GpioController gpio = GpioFactory.getInstance();

    final GpioPinDigitalInput letra0 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00);
    final GpioPinDigitalInput letra1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02);
    final GpioPinDigitalInput letra2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03);
    final GpioPinDigitalInput sensorPos = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07);
    final GpioPinDigitalInput sensorIn = gpio.provisionDigitalInputPin(RaspiPin.GPIO_20, PinPullResistance.PULL_DOWN);
    final GpioPinDigitalInput sensorOut = gpio.provisionDigitalInputPin(RaspiPin.GPIO_19, PinPullResistance.PULL_DOWN);

    final GpioPinDigitalOutput muxS0 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04);
    final GpioPinDigitalOutput muxS1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05);
    final GpioPinDigitalOutput muxS2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06);
    final GpioPinDigitalOutput enable = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);

    final GpioPinDigitalOutput esteira = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16);

    final GpioPinDigitalOutput stamps[] = new GpioPinDigitalOutput[]{
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10),
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11),
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12),
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13),
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14),};

    Skill[] skills;

//    public AcHw() {
//        this.skills = new Skill[]{
//            new Skill(this, "Stamp", "void", new String[]{"int"}) {
//                @Override
//                public void execute() throws SkillExecuteException {
//                    int mod = Integer.parseInt(getArgsValues()[0]);
//                    stamp(mod);
//                }
//            },
//        };
//    }
    public AcHw() {
        this.skills = new Skill[]{
            new Skill(this, "Stamp", "void", new String[]{"int"}) {
                @Override
                public void execute() throws SkillExecuteException {
                    int mod = Integer.parseInt(getArgsValues()[0]);
                    stamp(mod);
                }
            },
            new Skill(this, "MoveToStart", "void", new String[0]) {
                @Override
                public void execute() throws SkillExecuteException {
                    startConveryor();
                    waitInSensor();
                   // delay(2300);        //DEBUG
                    stopConveryor();
                }
            },
            new Skill(this, "MoveToEnd", "void", new String[0]) {
                @Override
                public void execute() throws SkillExecuteException {
                    startConveryor();
                    waitOutSensor();
                    stopConveryor();
                }
            },
            new Skill(this, "MoveTo", "void", new String[]{"int", "int"}) {
                @Override
                public void execute() throws SkillExecuteException {
                    int mod = Integer.parseInt(getArgsValues()[0]);
                    int pos = Integer.parseInt(getArgsValues()[1]);
                    moveTo(mod, pos);
                }
            },
            new Skill(this, "GetLetters", "void", new String[0]) {
                @Override
                public void execute() throws SkillExecuteException {
                    StringBuilder sb = new StringBuilder();
                    for (int mod = 0; mod < 5; mod++) {
                        sb.append(detectLetter(mod)).append(",");
                    }
                    setResult(sb.toString());
                    System.out.println("GetLetters : "+ sb.toString());
                }
            },};
    }

    private void delay(int millis) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
    }

    private void moveTo(int mod, int offset) {
        startConveryor();
        if (getSensorState(mod).isLow()) {
            for (; getSensorState(mod).isLow(););
        }
        for (; getSensorState(mod).isHigh(););
        for (int i = 1; i < offset; i++) {
            for (; getSensorState(mod).isLow(););
            for (; getSensorState(mod).isHigh(););
        }
        stopConveryor();
    }

    private void stamp(int mod) {
        stamps[mod].setState(PinState.HIGH);
        delay(500);
        stamps[mod].setState(PinState.LOW);
    }

    private void startConveryor() {
        esteira.setState(PinState.HIGH);

    }

    private void stopConveryor() {
        esteira.setState(PinState.LOW);
    }

    private void waitOutSensor() {
        do {
//            System.out.println("sensorOut");
        } while (sensorOut.isLow());
    }

    private void waitInSensor() {
        do {
//            System.out.println("sensorIn");
        } while (sensorIn.isLow());
    }

    private int c;
    private boolean b;

    private PinState getSensorState(int pos) {
        PinState state;
        switch (pos) {
            case 0:
                muxS0.setState(PinState.LOW);
                muxS1.setState(PinState.LOW);
                muxS2.setState(PinState.LOW);
                break;
            case 1:
                muxS0.setState(PinState.HIGH);
                muxS1.setState(PinState.LOW);
                muxS2.setState(PinState.LOW);
                break;
            case 2:
                muxS0.setState(PinState.LOW);
                muxS1.setState(PinState.HIGH);
                muxS2.setState(PinState.LOW);
                break;
            case 3:
                muxS0.setState(PinState.HIGH);
                muxS1.setState(PinState.HIGH);
                muxS2.setState(PinState.LOW);
                break;
            case 4:
                muxS0.setState(PinState.LOW);
                muxS1.setState(PinState.LOW);
                muxS2.setState(PinState.HIGH);
                break;
            case 5:
                muxS0.setState(PinState.HIGH);
                muxS1.setState(PinState.LOW);
                muxS2.setState(PinState.HIGH);
                break;
            default:
                muxS0.setState(PinState.HIGH);
                muxS1.setState(PinState.HIGH);
                muxS2.setState(PinState.HIGH);
        }
        enable.setState(PinState.LOW);
        state = sensorPos.getState();
        enable.setState(PinState.HIGH);
        return state;
    }

    private int countSensor(int mod) {
        if (getSensorState(mod).isLow()) {
            b = true;
        }

        if (b && getSensorState(mod).isHigh()) {
            c++;
            b = false;
        }
        return c;
    }

    private void writeAddr(int addr) {
        int b0 = addr & 0b00000001;
        int b1 = addr & 0b00000010;
        int b2 = addr & 0b00000100;

        if (b0 == 0) {
            muxS0.setState(PinState.LOW);
        } else {
            muxS0.setState(PinState.HIGH);
        }

        if (b1 == 0) {
            muxS1.setState(PinState.LOW);
        } else {
            muxS1.setState(PinState.HIGH);
        }

        if (b2 == 0) {
            muxS2.setState(PinState.LOW);
        } else {
            muxS2.setState(PinState.HIGH);
        }
    }

    private String detectLetter(int mod) {
        int codigo;

        writeAddr(mod);
        enable.setState(PinState.LOW);

        codigo = (letra0.isHigh() ? 1 : 0);
        codigo += (letra1.isHigh() ? 2 : 0);
        codigo += (letra2.isHigh() ? 4 : 0);

        enable.setState(PinState.HIGH);

        switch (codigo) {
            case 0:
                return "N.C.";
            case 1:
                return "A";
            case 2:
                return "F";
            case 3:
                return "E";
            case 4:
                return "T";
            case 5:
                return "U";
            case 6:
                return "M";
            default:
                return "Erro";
        }
    }

    @Override
    protected void init() {
        super.init();

        stopConveryor();

        c = 0;
        b = false;
        Debug.setDebugLevel(Debug.INFO);
    }

    @Override
    protected void takeDown() {
        super.takeDown();
        stopConveryor();
    }

    private void verifica(int mod) {
        startConveryor();
        for (; getSensorState(mod).isHigh(););
        stopConveryor();

        delay(100);
        startConveryor();
        for (; getSensorState(mod).isLow(););
        stopConveryor();

        delay(1000);
    }

    @Override
    protected void autorun() {

//        int offset = 0;

//        while (true) {
//            moveTo(0, offset);

            
             
            // ============   teste  AFMT ================
//            //A        
//            moveTo(0, 4);
//            delay(200);
//            stamp(0);
//            delay(200);
//            //F      
//            moveTo(1, 3);
//            delay(200);
//            stamp(1);
//            delay(200);
//            //M
//            moveTo(2, 2);
//            delay(200);
//            stamp(2);
//            delay(200);
//            //U
//            moveTo(3, 1);
//            delay(200);
//            stamp(3);
//            delay(200);

            
//  // ============   teste  AFMU ================
            //A        
//            moveTo(0, 4);
//            delay(200);
//            stamp(0);
//            delay(200);
//            //F      
//            moveTo(1, 3);
//            delay(200);
//            stamp(1);
//            delay(200);
//            //M
//            moveTo(2, 2);
//            delay(200);
//            stamp(2);
//            delay(200);
//            //U
//            moveTo(4, 1);
//            delay(200);
//            stamp(4);
//            delay(200);
//           
//            
//     

            // ============   teste  UFAM ================
//            //A        
//            moveTo(0, 2);
//            delay(200);
//            stamp(0);
//            delay(200);
//            //F      
//            moveTo(1, 3);
//            delay(200);
//            stamp(1);
//            delay(200);
//            //M
//            moveTo(2, 1);
//            delay(200);
//            stamp(2);
//            delay(200);
//            //U
//            moveTo(4, 4);
//            delay(200);
//            stamp(4);
//            delay(200);

//              // ============   teste  FTUA ================
            //A        
//            moveTo(0, 1);
//            delay(200);
//            stamp(0);
//            delay(200);
//            //F      
//            moveTo(1, 4);
//            delay(200);
//            stamp(1);
//            delay(200);
//            //T
//            moveTo(3, 3);
//            delay(200);
//            stamp(3);
//            delay(200);
//            //U
//            moveTo(4, 2);
//            delay(200);
//            stamp(4);
//            delay(200);

            
//             // ============   teste  AFME (Plug-and-Produce) ================
//            //A        
//            moveTo(0, 4);
//            delay(200);
//            stamp(0);
//            delay(200);
//            //F      
//            moveTo(1, 3);
//            delay(200);
//            stamp(1);
//            delay(200);
//            //M
//            moveTo(2, 2);
//            delay(200);
//            stamp(2);
//            delay(200);
//            //U
//            moveTo(4, 1);
//            delay(200);
//            stamp(4);
//            delay(200);
//         
            
                     
            // ============   =====================
            
            
//            offset++;
//
//            if (offset == 5) {
//                offset = 1;
//            }

//        }

//        while (true) {
//            for (int i = 0; i < 4; i++) {
//                verifica(i);
//                delay(100);
//                stamp(i);
//                
//            }

//          //delay(100);
//            //verifica(1);
//        }
//        
//        startConveryor();
//        waitInSensor();
//        delay(1000);
//        while (true) {
//            moveTo(1, 1);
//            delay(1000);
//
//            moveTo(1, 1);
//            delay(1000);
//
//            moveTo(1, 1);
//            delay(1000);
//
//            moveTo(1, 1);
//            delay(3000);
//        }
//        moveTo(2, 0);
//        delay(1000);
//        
//        moveTo(2, 1);
//        delay(1000);
//        
//        moveTo(3, 1);
//        delay(1000);
//        
//        moveTo(3, 3);
//        delay(1000);
//        
//        moveTo(4, 4);
//        
//        System.out.println("Módulos e letras: ");
//        for (int i = 0; i < 5; i++) {
//            System.out.println(String.format("Mod: %d,   Letra: %s", i, detectLetter(i)));
//        }
//        
//        while (true) {
//            System.out.println(String.format(
//                    "sensores: \t%s\t%s\t%s\t%s\t%s",
//                    (getSensorState(0).isHigh() ? "HIGH" : "LOW "),
//                    (getSensorState(1).isHigh() ? "HIGH" : "LOW "),
//                    (getSensorState(2).isHigh() ? "HIGH" : "LOW "),
//                    (getSensorState(3).isHigh() ? "HIGH" : "LOW "),
//                    (getSensorState(4).isHigh() ? "HIGH" : "LOW ")
//            ));
//            delay(200);
//       // }
//
//  //      delay(1000);
//
//        startConveryor();
//        waitOutSensor();
//        stopConveryor();
//
//        delay(1000);
//
//        startConveryor();
//        waitInSensor();
//        stopConveryor();
//
//        delay(1000);

//        for (int i = 0; i < 5; i++) {
//            stamp(i);
//            delay(500);
//        }
//    }
    }

    @Override
    protected Skill[] getSkills() {
        return skills;
    }

    @Override
    protected MRAInfo getMRAInfo() {
        MRAInfo mraInfo = new MRAInfo();
        mraInfo.setName(this.getLocalName());
        mraInfo.setProperties(new String[0]);
        mraInfo.setSkills(Util.fromSkill(skills));

        return mraInfo;
    }

}
