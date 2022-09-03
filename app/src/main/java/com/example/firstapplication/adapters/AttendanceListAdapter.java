package com.example.firstapplication.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstapplication.R;
import com.example.firstapplication.entity.Attendance;

import java.util.List;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.MyViewHolder> {
    private List<Attendance> attendanceList;

    public AttendanceListAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(viewItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String info = attendanceList.get(position).getInfo();
        String scannedDate = attendanceList.get(position).getScannedDate();
        String type = attendanceList.get(position).getType();
        String status = attendanceList.get(position).getSynced().toString();
        if ("true".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Đã đồng bộ");
            holder.tvStatus.setTextColor(Color.parseColor("#FF018786"));
        }

        holder.tvInfo.setText(info);
        holder.tvScannedDate.setText(scannedDate);

        holder.tvType.setText("Loại quét: " + type);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), info, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public void updateDataset(List<Attendance> newList){
        this.attendanceList.clear();
        this.attendanceList.addAll(newList);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout relativeLayout;
        private TextView tvInfo;
        private TextView tvType;
        private TextView tvScannedDate;
        private TextView tvStatus;

        public MyViewHolder(final View view) {
            super(view);
            relativeLayout = view.findViewById(R.id.relativeLayout);
            tvInfo = view.findViewById(R.id.tvItemInfo);
            tvScannedDate = view.findViewById(R.id.tvItemScannedDate);
            tvType = view.findViewById(R.id.tvItemType);
            tvStatus = view.findViewById(R.id.tvItemStatus);
        }
    }
}
