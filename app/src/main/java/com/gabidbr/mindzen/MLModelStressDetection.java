package com.gabidbr.mindzen;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MLModelStressDetection {

    private static final float[] MEAN = {4.98461538e+03f, 4.95769231e+00f, 3.87153846e+01f, 4.92307692e-01f};
    private static final float[] STD_DEV = {1.86252358e+03f, 2.09581507e+00f, 1.30096027e+01f, 4.99940825e-01f};

    private static MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        FileInputStream inputStream = new FileInputStream(modelPath);
        FileChannel fileChannel = inputStream.getChannel();
        long fileSize = fileChannel.size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
    }

    public static void useMLModel() {
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("stress-model", DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        // Download complete. Depending on your app, you could enable
                        // the ML feature, or switch from the local model to the remote
                        // model, etc.
                        try {
                            // Load the TensorFlow Lite model
                            MappedByteBuffer tfliteModel = loadModelFile(model.getLocalFilePath());
                            Interpreter.Options options = new Interpreter.Options();
                            Interpreter tflite = new Interpreter(tfliteModel, options);

                            // Prepare input data for a new user
                            float[][] input = new float[1][4];
                            input[0][0] = 3000; // numar_pasi
                            input[0][1] = 9;    // timp_mediu_telefon
                            input[0][2] = 39;   // varsta
                            input[0][3] = 1;    // sex (feminin)

                            // Normalize the input data using the same scaler values you used in the training script
                            for (int i = 0; i < input[0].length; i++) {
                                input[0][i] = (input[0][i] - MEAN[i]) / STD_DEV[i];
                            }

                            // Run inference
                            float[][] output = new float[1][1];
                            tflite.run(input, output);

                            // Get the stress level
                            float stressLevel = output[0][0];
                            System.out.println("Stresslevel: " + stressLevel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
