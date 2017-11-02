package models;

import com.google.common.collect.MinMaxPriorityQueue;
import util.Utils;

import java.util.*;

public class NavigationTable {
    public static final int MAX_PATHS_FOR_DESTINATION = 3;

    private class SrcDestTuple {
        private String srcId = Utils.EMPTY_STRING;
        private String destId = Utils.EMPTY_STRING;

        public SrcDestTuple(String srcId, String destId) {
            this.srcId = srcId;
            this.destId = destId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SrcDestTuple)) return false;

            SrcDestTuple that = (SrcDestTuple) o;

            if (!srcId.equals(that.srcId)) return false;
            return destId.equals(that.destId);
        }

        @Override
        public int hashCode() {
            int result = srcId.hashCode();
            result = 31 * result + destId.hashCode();
            return result;
        }
    }


    private class NavigationTableEntry {

        private MinMaxPriorityQueue<UIPath> uiPathQueue;

        public NavigationTableEntry() {
            uiPathQueue = MinMaxPriorityQueue.orderedBy(new UIPath.UIPathComparator())
                    .maximumSize(MAX_PATHS_FOR_DESTINATION)
                    .create();
        }

        public boolean addPath(UIPath uiPath) {
            return uiPathQueue.add(uiPath);
        }

        public UIPath getShortestPath() {
            return uiPathQueue.peekFirst();
        }

        public List<UIPath> getAllPaths() {
            return Arrays.asList((UIPath [])uiPathQueue.toArray());
        }
    }

    HashMap<SrcDestTuple, NavigationTableEntry> uiPaths;

    public NavigationTable() {
        uiPaths = new HashMap<>();
    }

    public boolean addUIPath(String srcId, String destId, UIPath uiPath) {
        if (Utils.nullOrEmpty(srcId) || Utils.nullOrEmpty(destId)) {
            return false;
        }
        boolean addResult = false;
        SrcDestTuple srcDestTuple = new SrcDestTuple(srcId, destId);
        NavigationTableEntry entry = uiPaths.get(srcDestTuple);
        if (entry == null) {
            entry = new NavigationTableEntry();
            uiPaths.put(srcDestTuple, entry);
        }
        return entry.addPath(uiPath);
    }

    public UIPath getShortestPath(String srcId, String destId) {
        NavigationTableEntry entry = uiPaths.get(new SrcDestTuple(srcId, destId));
        if (entry == null) {
            return null;
        }
        return entry.getShortestPath();
    }

    public int length() {
        return uiPaths.size();
    }

}
