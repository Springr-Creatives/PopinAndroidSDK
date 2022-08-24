package to.popin.androidsdk.call;

import java.util.concurrent.Executor;



public class CallRepository {
    private final Executor executor;



    public CallRepository(Executor executor) {
        this.executor = executor;
        //this.talkDAO = talkDAO;
    }


    public void markCallAttended(int call_id) {
//        executor.execute(() -> {
//            if (!talkDAO.isTalkExists(call_id)) {
//                talkDAO.updateStatus(call_id, 1);
//            }
//        });
    }

    public void markCallEnded(int call_id) {
//        executor.execute(() -> {
//            if (talkDAO.isTalkExists(call_id)) {
//                talkDAO.updateStatus(call_id, 4);
//            }
//        });
    }

}
