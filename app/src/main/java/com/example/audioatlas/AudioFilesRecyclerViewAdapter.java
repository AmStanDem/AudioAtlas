package com.example.audioatlas;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static androidx.core.content.ContextCompat.startActivity;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.RingtonePreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.MimeTypeFilter;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AudioFilesRecyclerViewAdapter extends RecyclerView.Adapter<AudioFilesRecyclerViewAdapter.ViewHolder> {

    private  final List<File> mData;
    private final LayoutInflater mInflater;
    private static ItemClickListener mClickListener;

    private int selectedPosition = -1;

    private int previousPosition = -1;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ViewHolder(view, context);
        }
        return null;
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
        // variable to hold context
        private Context context;
        public  File file;


        @RequiresApi(api = Build.VERSION_CODES.Q)
        ViewHolder(View itemView , Context context) {
            super(itemView);
            setElementIds(itemView);
            this.context = context;
            btnSettings.setOnClickListener(v -> {
                // 1. Instantiate an AlertDialog.Builder with its constructor.
                AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

                String[] choices = {"Condividi/Inviare", "Rinominare", "Eliminare","Aprire con"};

// 2. Chain together various setter methods to set the dialog characteristics.
                        builder
                        .setTitle(textViewFileName.getText())
                        .setPositiveButton("Ok", (dialog, which) -> {

                            // user clicked OK
                            int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                            // do something with the selection
                            Log.println(Log.DEBUG, "d", String.valueOf(selectedPosition));

                            file = mData.get(getAdapterPosition());
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
                                    AlertDialog.Builder renameBuilder = new AlertDialog.Builder(context);
                                    renameBuilder
                                            .setTitle(textViewFileName.getText())
                                            .setMessage("Rinomina");
                                    // Set an EditText view to get user input
                                    final EditText input = new EditText(context);
                                    renameBuilder.setView(input);
                                    renameBuilder.setPositiveButton("Ok", (renameDialog, renameWhich) -> {
                                        String newName = input.getText().toString();
                                        if (newName.isEmpty())
                                        {
                                            Toast.makeText(context, "Please enter a valid file name", Toast.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            newName+=".mp3";
                                            File Directory = context.getFilesDir();
                                            File newFile = new File(newName);
                                            if (Directory.exists())
                                            {
                                                // Rename the old file to the new file
                                                if (file.exists()) {
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                        Path source = Paths.get(file.toURI());
                                                        try {
                                                            Path newPath = Files.move(source, source.resolveSibling(newName), StandardCopyOption.ATOMIC_MOVE);
                                                            mData.set(this.getAdapterPosition(), newPath.toFile());
                                                            notifyItemChanged(getAdapterPosition());
                                                        } catch (IOException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    AlertDialog renameDialog = renameBuilder.create();
                                    renameDialog.show();

                                    break;
                                }
                                case "Eliminare":
                                {
                                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
                                    deleteBuilder.setTitle("Elimina");
                                    deleteBuilder.setMessage("Sei sicuro?");
                                    deleteBuilder.setPositiveButton("Ok", (deleteDialog, deleteWhich)->{
                                        removeItem(this.getAdapterPosition(), file);
                                    });
                                    AlertDialog removeDialog = deleteBuilder.create();
                                    removeDialog.show();
                                    break;
                                }
                                case "Aprire con":
                                {
                                    // Open file with user selected app
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                                    String mime = context.getContentResolver().getType(fileUri);
                                    intent.setDataAndType(fileUri, mime);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    startActivity(context ,intent,null);
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
        public void onClick(View view)
        {

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

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public List<File> getmData() {
        return mData;
    }

    private void removeItem(int position, File fileToDelete) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            try {
                Files.delete(fileToDelete.toPath());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }



    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onPrepared(MediaPlayer mp);

        void onItemClick(View view, int position);
    }
}
