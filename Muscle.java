package com.ede1998.genalg;

/**
 * Created by Erik on 19.03.2017.
 */
public class Muscle {
    private static final double MAX_LENGTH = 12;
    private static final double MAX_STRENGTH = 10;
    private double strength;
    private double contractedLength;
    private double extendedLength;
    private double timeContractionStart;
    private double timeExtensionStart;
    private double length;

    private Muscle(double length, double contractedLength, double extendedLength, double timeContractionStart, double timeExtensionStart, double strength) {
        this.strength = strength;
        this.contractedLength = contractedLength;
        this.extendedLength = extendedLength;
        if (contractedLength > extendedLength)
            throw new IllegalArgumentException("Extended muscle must be longer than contracted muscle.");
        if ((timeContractionStart < 0) || (timeContractionStart > 1)) {
            throw new IllegalArgumentException("timeContractionStart must be between 0 and 1.");
        }
        if ((timeExtensionStart < 0) || (timeExtensionStart > 1)) {
            throw new IllegalArgumentException(("timeExtensionStart must be between 0 and 1."));
        }
        if (timeContractionStart == timeExtensionStart) {
            throw new IllegalArgumentException("timeExtensionStart must differ from timeContractionStart.");
        }

        this.timeContractionStart = timeContractionStart;
        this.timeExtensionStart = timeExtensionStart;
        this.length = length;
    }

    public Muscle(double length) {
       this(length, length - RandomNumberGenerator.random(length),
                        length + RandomNumberGenerator.random(Muscle.MAX_LENGTH - length),
                        RandomNumberGenerator.random(), RandomNumberGenerator.random(), RandomNumberGenerator.random(MAX_STRENGTH));
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getStrength() {
        return strength;
    }

    public boolean isContracted(double timeQuotient) {
        if ((timeQuotient > 1) || (timeQuotient < 0))
            throw new IllegalArgumentException("timeQuotient must be between 0 and 1.");
        if (timeQuotient == timeContractionStart) {
            return true;
        }
        // equal to extension
        else if (timeQuotient == timeExtensionStart) {
            return false;
        }
        //smaller than both
        else if ((timeQuotient < timeExtensionStart) && (timeQuotient < timeContractionStart)) {
            //it's contraction time
            return timeExtensionStart < timeContractionStart;
        }
        //larger than both
        else if ((timeQuotient > timeExtensionStart) && (timeQuotient > timeContractionStart)) {
            //it's contraction time
            return timeExtensionStart < timeContractionStart;
        }
        //between both
        else {
            //it's contraction time
            return timeExtensionStart < timeContractionStart;
        }
    }
    /*public void tenseOrRelease(double timeQuotient) {
        length = calcLengthQuotient(timeQuotient) * (extendedLength - contractedLength) + contractedLength;
    }

    private double calcLengthQuotient(double timeQuotient) {
        if ((timeQuotient > 1) || (timeQuotient < 0))
            throw new IllegalArgumentException("timeQuotient must be between 0 and 1.");
        //equal to contraction
        if (timeQuotient == timeContractionStart) {
            return 1;
        }
        // equal to extension
        else if (timeQuotient == timeExtensionStart) {
            return 0;
        }
        //smaller than both
        else if ((timeQuotient < timeExtensionStart) && (timeQuotient < timeContractionStart)) {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart) {
                return (1 - timeContractionStart + timeQuotient) / (1 - (timeContractionStart - timeExtensionStart));
            } else //C < E => it's extension time
            {
                return (1 - timeExtensionStart + timeQuotient) / (1 - (timeExtensionStart - timeContractionStart));
            }
        }
        //larger than both
        else if ((timeQuotient > timeExtensionStart) && (timeQuotient > timeContractionStart)) {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart) {
                return (timeQuotient - timeContractionStart) / (1 - (timeContractionStart - timeExtensionStart));
            } else //C < E => it's extension time
            {
                return (timeQuotient - timeExtensionStart) / (1 - (timeExtensionStart - timeContractionStart));
            }
        }
        //between both
        else {
            //it's contraction time
            if (timeExtensionStart < timeContractionStart) {
                return (timeQuotient - timeExtensionStart) / (timeContractionStart - timeExtensionStart);
            } else //C < E => it's extension time
            {
                return 1 - ((timeQuotient - timeContractionStart) / (timeExtensionStart - timeContractionStart));
            }
        }
    }*/

    @Override
    public Muscle clone() {
        return new Muscle(length, contractedLength, extendedLength, timeContractionStart, timeExtensionStart, strength);
    }

    public void mutate(double divergence, double startingDist) {
        contractedLength *= RandomNumberGenerator.randG(divergence, 1);
        extendedLength *= RandomNumberGenerator.randG(divergence, 1);
        if (contractedLength > startingDist) contractedLength = startingDist;
        if (extendedLength < startingDist) extendedLength = startingDist;
        length = startingDist;

        timeContractionStart *= RandomNumberGenerator.randG(divergence, 1);
        while (timeContractionStart >= 1) timeContractionStart--;
        while (timeContractionStart < 0) timeContractionStart++;
        timeExtensionStart *= RandomNumberGenerator.randG(divergence, 1);
        if (timeExtensionStart >= 1) timeExtensionStart--;
        if (timeExtensionStart < 0) timeExtensionStart++;

        strength *= RandomNumberGenerator.randG(divergence, 1);
    }
}