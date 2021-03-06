import ai.preprocessing.Dataloader;
import ai.preprocessing.FeatureScaling;
import ai.models.LogisticRegression;
import ai.metrics.ModelEvaluator;
import org.math.plot.*;
import org.math.plot.plotObjects.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;



public class RecallPrecisiongraph
{
  public static void main (String[] args)
  {
    //normal log regression stuff:
    Dataloader data = new Dataloader();

    data.makeArrays(args[0]);
    data.shuffleData();
    data.trainTestSplit(0.70);

    double [][] x_train, x_test;
    double [] y_train, y_test;

    x_train = data.returnXTrainArray();
    x_test = data.returnXTestArray();
    y_train = data.returnYTrainArray();
    y_test = data.returnYTestArray();

    FeatureScaling.standardScaler(x_train, x_test);

    LogisticRegression classifier = new LogisticRegression(x_train, x_test, y_train, y_test);

    classifier.fit(0.001,10000, false); //alpha, maxiterations, randomize intial weights or not, check for tolerance level

    double[] recallPrecisionVars;
    double[] predictionsOnTestSet;
    double[] recall = new double[100];
    double[] precision = new double[100];
    int count = 0;

    //now we'll iterate through a bunch of different thresholds of prediction, and get the recall and precision
    //using a custom method.
    
    for(double threshold = 0.0; threshold < 1.0; threshold += 0.01)
    {
      predictionsOnTestSet = classifier.predictTestSet(threshold);

      recallPrecisionVars = getRecallPrecisionVariables(y_test, predictionsOnTestSet);

      recall[count] = recallPrecisionVars[0];
      precision[count] = recallPrecisionVars[1];

      count++;
    }

    //Create plot
    Plot2DPanel panel = new Plot2DPanel();

    panel.addLinePlot("RecallPrecision curve", recall, precision);
    panel.setAxisLabel(0, "Recall");
    panel.getAxis(0).setLabelPosition(0.5,-0.1);
    panel.setAxisLabel(1, "Precision");
    panel.getAxis(1).setLabelPosition(-0.15,0.5);

    BaseLabel title = new BaseLabel("Recall-Precision graph for Logistic Regression", Color.BLUE, 0.5, 1.1);
    title.setFont(new Font("Courier", Font.BOLD, 16));
    panel.addPlotable(title);

    JFrame frame = new JFrame("Output of RecallPrecisiongraph.java");
    frame.setSize(525, 525);
    frame.setContentPane(panel);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);




  }

  public static double[] getRecallPrecisionVariables(double[] yLabels, double[] predictedYLabels)
  {
    int tp = 0; //true positives
    int fp = 0; //false positives
    int fn = 0; //false negatives
    int actualZero = 0;
    int actualOne = 0;
    int predictedOne = 0;

    double[] var = new double[2];

    for(int i = 0; i < predictedYLabels.length; i++)
    {
      //true positive
      if ((yLabels[i] == predictedYLabels[i]) && (yLabels[i] == 1))
      {
        tp++;
        actualOne++;
      }
      //true negatives
      else if ((yLabels[i] == predictedYLabels[i]) && (yLabels[i] == 0))
      {
        actualZero++;
      }
      //false positives
      else if ((yLabels[i] == 0) && (predictedYLabels[i] == 1))
      {
        fp++;
        actualZero++;
      }
      //false negatives
      else if ((yLabels[i] == 1) && (predictedYLabels[i] == 0))
      {
        fn++;
        actualOne++;
      }
    }

    var[0] = (double)tp/Math.max(1, (tp+fn));
    var[1] = (double)tp/Math.max(1,(tp+fp));

    return var;

  }
}
