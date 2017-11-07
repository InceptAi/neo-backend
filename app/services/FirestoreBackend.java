package services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import models.UIScreen;
import util.Utils;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Singleton
public class FirestoreBackend implements DatabaseBackend {
    private static final String FIREBASE_PROJECT_ID = "neobackend";
    private static final String UI_SCREEN_ENDPOINT = "uiscreens";

    private Firestore firestoreDatabase;
    public FirestoreBackend() {
        FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId(FIREBASE_PROJECT_ID)
                        .build();
        firestoreDatabase = firestoreOptions.getService();
    }

    @Override
    public UIScreen getScreenById(String screenId) {
        DocumentReference screenRef = firestoreDatabase.collection(UI_SCREEN_ENDPOINT).document(screenId);
        // asynchronously retrieve the document
        ApiFuture<DocumentSnapshot> future = screenRef.get();
        // block on response
        DocumentSnapshot documentSnapshot = null;
        try {
            documentSnapshot = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        UIScreen uiScreen = null;
        if (documentSnapshot != null && documentSnapshot.exists()) {
            // convert document to POJO
            uiScreen = documentSnapshot.toObject(UIScreen.class);
            Utils.printDebug("Fetched screen with ID: " + screenId + "  screen: " + uiScreen.toString());
        } else {
            Utils.printDebug("No screen with ID: " + screenId);
        }
        return uiScreen;
    }

    @Override
    public List<UIScreen> getAllScreens() {
        // asynchronously retrieve all users
        ApiFuture<QuerySnapshot> future = firestoreDatabase.collection(UI_SCREEN_ENDPOINT).get();
        // ...
        // query.get() blocks on response
        List<DocumentSnapshot> documentSnapshotList = null;
        try {
            documentSnapshotList = future.get().getDocuments();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (documentSnapshotList == null) {
            return new ArrayList<>();
        }

        List<UIScreen> uiScreenList = new ArrayList<>();
        for (DocumentSnapshot document : documentSnapshotList) {
            UIScreen uiScreen = document.toObject(UIScreen.class);
            Utils.printDebug(document.getId() + " => " + uiScreen);
            uiScreenList.add(uiScreen);
        }
        return uiScreenList;
    }

    @Override
    public List<UIScreen> getAllScreensForDevice(String deviceInfo) {
        return null;
    }

    @Override
    public List<UIScreen> getAllScreensForPackage(String packageName) {
        return null;
    }

    @Override
    public void saveScreens(List<UIScreen> uiScreenList) {
        for (UIScreen uiScreen: uiScreenList) {
            String screenId = uiScreen.getId();
            DocumentReference docRef = firestoreDatabase.collection(UI_SCREEN_ENDPOINT).document(screenId);
            //asynchronously write data
            ApiFuture<WriteResult> result = docRef.set(uiScreen);
            // result.get() blocks on response
            try {
                System.out.println("Update time : " + result.get().getUpdateTime());
            } catch (InterruptedException|ExecutionException e) {
                Utils.printDebug("Exception in reading back screen data");
            }
        }
    }
}
