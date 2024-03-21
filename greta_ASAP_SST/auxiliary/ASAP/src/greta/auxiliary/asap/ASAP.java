/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.asap;

import greta.core.util.CharacterManager;


import java.util.Arrays;

import java.util.logging.Logger;

import greta.core.keyframes.face.AUEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.repositories.AULibrary;
import greta.core.util.enums.Side;

/**
 *
 * @author Michele Grimaldi
 * @author Jieyeon Woo
 */
public class ASAP implements Runnable{

    
    protected static final Logger LOGGER = Logger.getLogger(ASAP.class.getName());
    
    protected ASAPFrame loader;
    
    public CharacterManager cm;
    
    protected Thread thread;
    private AUEmitterImpl auEmitter = new AUEmitterImpl();
    private BAPFrameEmitterImpl bapFrameEmitter = new BAPFrameEmitterImpl();
    protected final String threadName = ASAP.class.getSimpleName();
    protected double frameDuration = 0.0;
    protected ASAPFrame curFrame = new ASAPFrame();
    protected ASAPFrame prevFrame = new ASAPFrame();
    protected double fps = 0.0;
    protected double min_time = Double.MAX_VALUE;
    protected double max_time = 0.0;	
    protected boolean useFilter = true;	
    protected ArrayOfDoubleFilterPow filterAUs = new ArrayOfDoubleFilterPow(64,5,.5);
    protected ArrayOfDoubleFilterPow filterBAP = new ArrayOfDoubleFilterPow(3,5,.5);

    protected double alpha = 0.75; //1.0;
    
    private int startInputFrame = 0;
    private final int offsetFrame = 0;
    
    protected double prev_rot_X = 0.0;
    protected double prev_rot_Y = 0.0;
    protected double prev_rot_Z = 0.0;
    
    private AULibrary auLibrary;
    private static Side[] wantedSides = {Side.RIGHT, Side.LEFT};

    protected String[] selectedFeatures;
    /**
     * @param loader
     * @param cm
     * @param args the command line arguments
     */
    
    public ASAP(CharacterManager cm, ASAPFrame loader){
        this.cm=cm;
        this.loader=loader;
        this.auLibrary = new AULibrary(cm);
        
    }
    
    
    public void setSelected(String[] selected) {
        if (selected != null) {
            if (!Arrays.equals(selected, selectedFeatures)) {
                selectedFeatures = selected;
                setSelectedFeatures(selectedFeatures);
            }
            getLogger().info(String.format("Setting selected features to: %s", Arrays.toString(selected)));
        } else {
            getLogger().warning("No header selected");
        }
    }
    public boolean isUseFilter() {
        return useFilter;
    }

    /* ---------------------------------------------------------------------- */
    
    

    
        public String[] getSelectedFeatures() {
        return selectedFeatures;
    }

    /**
     * @param features the selected output features to set
     */
    public void setSelectedFeatures(String[] features) {
        selectedFeatures = features;
    }
    protected Logger getLogger() {
        return LOGGER;
    }
    
    

@Override
    public void run() {
        LOGGER.info(String.format("Thread: %s running", ASAP.class.getName()));
        // Socket Connection
    }
//
//@Override    
//public void performAUAPFrame (AUAPFrame auapAnimation, ID requestId) {
//    sendFAPFrame(requestId, toFAPFrame_ASAP(auapAnimation));
//}
//
//@Override
//    public void performAUAPFrames (List<AUAPFrame> auapsAnimation, ID requestId) {
//        ArrayList<FAPFrame> fapFrames = new ArrayList<>(auapsAnimation.size());
//        for (AUAPFrame auFrame : auapsAnimation) {
//            fapFrames.add(toFAPFrame_ASAP(auFrame));
//        }
//        sendFAPFrames(requestId, fapFrames);
//    }

//public FAPFrame toFAPFrame_ASAP(AUAPFrame auFrame) {
//        FAPFrame min = new FAPFrame(auFrame.getFrameNumber());
//        FAPFrame max = new FAPFrame(auFrame.getFrameNumber());
//        int[] upau_list = {1,2,4,5,6,7,43,61,62,63,64};
//        
//        for (int upau = 0; upau < upau_list.length; upau++) {
//            int auNb = upau_list[upau];
//            if (auFrame.useActionUnit(auNb)) {
//                FLExpression expression = auLibrary.findExpression("AU" + auNb);
//                if (expression != null) {
//                    List<FLExpression.FAPItem> auFaps = expression.getFAPs();
//                    for (Side side : wantedSides) {
//                        AUAP auap = auFrame.getAUAP(auNb, side);
//                        if (auap.getMask()) {
//                            double auapNormalizedValue = auap.getNormalizedValue();
//                            for (FLExpression.FAPItem fap : auFaps) {
//                                if (((side == Side.RIGHT) && fap.type.isRight()) || ((side == Side.LEFT) && fap.type.isLeft())) {
//                                    int fapIntensity = (int) (fap.value * auapNormalizedValue);
//                                    //find max
//                                    if ((!max.getMask(fap.type)) || max.getValue(fap.type) < fapIntensity) {
//                                        max.applyValue(fap.type, fapIntensity);
//                                    }
//                                    //find min
//                                    if ((!min.getMask(fap.type)) || min.getValue(fap.type) > fapIntensity) {
//                                        min.applyValue(fap.type, fapIntensity);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < FAPType.NUMFAPS; i++) {
//            if (min.getMask(i)) {
//                if (max.getMask(i)) {
//                    //if there is only positive values, we use the max.
//                    if (min.getValue(i) < 0) {
//                        if (max.getValue(i) <= 0) {
//                            //if there is only negative values, we use the min. (max of the absolute values)
//                            max.applyValue(i, min.getValue(i));
//                        } else {
//                            //if there is positive AND negative values, we use the sum of min and max.
//                            max.applyValue(i, min.getValue(i) + max.getValue(i));
//                        }
//                    }
//                } else {
//                    max.applyValue(i, min.getValue(i));
//                }
//            }
//        }
//        return max;
//    }

//    @Override
//    public void cancelAUKeyFramesById(ID requestId) {
//        cancelFramesWithIDInLinkedPerformers(requestId);
//    }
//


}
