package services;

import akka.actor.ActorSystem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import models.UIScreen;
import play.libs.concurrent.CustomExecutionContext;
import util.Utils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@SuppressWarnings("unused")
@Singleton
public class FirestoreBackend implements DatabaseBackend {
    private static final String FIREBASE_PROJECT_ID = "neobackend";
    private static final String UI_SCREEN_ENDPOINT = "uiscreens";

    private Firestore firestoreDatabase;

    @Inject
    private FirestoreExecutionContext executionContext;

    private static class FirestoreExecutionContext extends CustomExecutionContext {

        @Inject
        public FirestoreExecutionContext(ActorSystem actorSystem) {
            super(actorSystem, "load-from-database-dispatcher");
        }
    }

    @Inject
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
    public <U> void loadAllScreens(Function<List<UIScreen>, U> function) {
        ApiFuture<QuerySnapshot> future = firestoreDatabase.collection(UI_SCREEN_ENDPOINT).get();
        future.addListener(() -> {
            try {
                function.apply(parse(future.get().getDocuments()));
            } catch (Exception e) {
                // log error.
                e.printStackTrace();
                Utils.printDebug(e.toString());
            }
        }, executionContext);
    }

    private static List<UIScreen> parse(List<DocumentSnapshot> documentSnapshotList) {
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
    public List<UIScreen> getAllScreens() {
        ApiFuture<QuerySnapshot> future = firestoreDatabase.collection(UI_SCREEN_ENDPOINT).get();
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
    public void saveScreensAsync(List<UIScreen> uiScreenList) {
        if (uiScreenList == null || uiScreenList.isEmpty()) {
            return;
        }
        // Get a new write batch
        WriteBatch batch = firestoreDatabase.batch();
        for (UIScreen uiScreen : uiScreenList) {
            String screenId = uiScreen.getId();
            DocumentReference docRef = firestoreDatabase.collection(UI_SCREEN_ENDPOINT).document(screenId);
            batch.set(docRef, uiScreen);
            // asynchronously commit the batch
            //ApiFuture<List<WriteResult>> future = batch.commit();
            // ...
            // future.get() blocks on batch commit operation
            //for (WriteResult result :future.get()) {
            //    System.out.println("Update time : " + result.getUpdateTime());
            //}
        }
        batch.commit();
    }
}
