package ehud.marchi.astromusic;



import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SongMoveCallback extends ItemTouchHelper.Callback {

    private final SongTouchHelperContract mAdapter;

    public SongMoveCallback(SongTouchHelperContract adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }



    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        mAdapter.onRowSwipeRight((SongAdapter.SongViewHolder) viewHolder);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlag = ItemTouchHelper.RIGHT;

        return makeMovementFlags(dragFlags, swipeFlag);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                  int actionState) {

        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof SongAdapter.SongViewHolder) {
                SongAdapter.SongViewHolder myViewHolder= (SongAdapter.SongViewHolder) viewHolder;
                mAdapter.onRowSelected(myViewHolder);
            }

        }

        super.onSelectedChanged(viewHolder, actionState);
    }
    @Override
    public void clearView(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof SongAdapter.SongViewHolder) {
            SongAdapter.SongViewHolder myViewHolder=
                    (SongAdapter.SongViewHolder) viewHolder;
            mAdapter.onRowClear(myViewHolder);
        }
    }

    public interface SongTouchHelperContract {

        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(SongAdapter.SongViewHolder myViewHolder);
        void onRowClear(SongAdapter.SongViewHolder myViewHolder);
        void onRowSwipeRight(SongAdapter.SongViewHolder myViewHolder);

    }

}
