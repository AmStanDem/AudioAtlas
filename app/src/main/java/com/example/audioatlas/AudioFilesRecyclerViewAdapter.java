package com.example.audioatlas;






import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AudioFilesRecyclerViewAdapter extends RecyclerView.Adapter<AudioFilesRecyclerViewAdapter.ViewHolder> {

    private final List<File> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    // data is passed into the constructor
    AudioFilesRecyclerViewAdapter(Context context, ArrayList<File> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.rv_row, parent, false);
        return new ViewHolder(view, mData.get(parent.getVerticalScrollbarPosition()), context);
    }

    // binds the data to the TextView in each row
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File audioFile = mData.get(position);
        holder.textViewFileName.setText(audioFile.getName());

        String textAudioLen = String.valueOf(getAudioLength(audioFile));
        holder.durationTextView.setText(textAudioLen);

        String textAudioDate = getAudioCreationDateToString(audioFile);
        holder.textViewDate.setText(textAudioDate);

        String textAudioSize = String.valueOf(getAudioSize(audioFile));
        holder.textViewFileSize.setText(textAudioSize +" bytes" );
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewFileName;
        private TextView durationTextView;
        private TextView textViewDate , textViewFileSize;
        private ImageButton btnSettings;
        private File file;
        // variable to hold context
        private Context context;


        ViewHolder(View itemView, File file, Context context) {
            super(itemView);
            setElementIds(itemView);
            this.file = file;
            this.context = context;
            btnSettings.setOnClickListener(v -> {
                // 1. Instantiate an AlertDialog.Builder with its constructor.
                AlertDialog.Builder builder = new AlertDialog.Builder(this.btnSettings.getContext());

                String[] choices = {"Condividi/Inviare", "Rinominare", "Eliminare", "Imposta come", "Aprire con"};

// 2. Chain together various setter methods to set the dialog characteristics.
                builder
                        .setTitle(textViewFileName.getText())
                        .setPositiveButton("Ok", (dialog, which) -> {

                            // user clicked OK
                            int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                            // do something with the selection
                            Log.println(Log.DEBUG, "d", String.valueOf(selectedPosition));

                            switch (choices[selectedPosition])
                            {
                                case "Condividi/Inviare":
                                {


                                    if(file.exists()) {
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("audio/mp3");
                                        share.putExtra(Intent.EXTRA_STREAM,  FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file));
                                        startActivity(context, share, null);
                                    }
                                    break;
                                }
                                case "Rinominare":
                                {
                                    System.out.printf("Rinomina");
                                    break;
                                }
                                case "Eliminare":
                                {
                                    System.out.printf("Elimina");
                                    break;
                                }
                                case "Imposta come":
                                {
                                    System.out.printf("Imposta come");
                                    break;
                                }
                                case "Aprire con":
                                {
                                    System.out.printf("Apri");
                                    break;
                                }
                                default:
                                {
                                    break;
                                }
                            }
                        })
                        .setNegativeButton("Annulla", (dialog, which) -> {
                            dialog.cancel();
                        })
                        .setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

// 3. Get the AlertDialog.
                AlertDialog dialog = builder.create();
                dialog.show();
            });
            itemView.setOnClickListener(this);
        }

        private void setElementIds(View itemView)
        {
            textViewFileName = itemView.findViewById(R.id.tv_file_name);
            textViewDate = itemView.findViewById(R.id.tv_file_date);
            textViewFileSize = itemView.findViewById(R.id.tv_file_size);
            durationTextView = itemView.findViewById(R.id.tv_duration);
            btnSettings = itemView.findViewById(R.id.btn_settings);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
            {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public String getAudioLength(File audioFile)
    {
        try (MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever())
        {
            metadataRetriever.setDataSource(audioFile.getAbsolutePath());
            double duration = Double.parseDouble(Objects.requireNonNull(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            duration = duration / 1000;
            duration = Math.ceil(duration);
            return secondsToHoursMinutesAndSecondsToString((long) duration);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private String secondsToHoursMinutesAndSecondsToString(long durationInSeconds)
    {
        int hours = (int) durationInSeconds / 3600;
        int remainder = (int) durationInSeconds - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        int seconds = remainder;

        return hours + ":"+minutes + ":" + seconds;

    }

    @NonNull
    private String getAudioCreationDateToString(File audioFile)
    {
        try {
            Path file = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                file = audioFile.toPath();
            }
            BasicFileAttributes attr = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                attr = Files.readAttributes(file, BasicFileAttributes.class);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return attr.creationTime().toString();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private long getAudioSize(File audioFile)
    {
        try {
            Path file = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                file = audioFile.toPath();
            }
            BasicFileAttributes attr = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                attr = Files.readAttributes(file, BasicFileAttributes.class);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return attr.size();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    // convenience method for getting data at click position
    File getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
