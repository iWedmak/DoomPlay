package com.perm.DoomPlay;


/*
 *    Copyright 2013 Vladislav Krot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    You can contact me <DoomPlaye@gmail.com>
 */
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
    dialog is used for add playlist , add album , edit album.
 */

abstract class AddListDialog extends DialogFragment
{


    private EditText editNewDialog;
    private TextView textInvalid;

    abstract boolean isPlaylistExist(String playlist);
    abstract void createPlatlist(String playlist);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view  = inflater.inflate(R.layout.dialog_addlist,container,false);
        Button buttonCancel = (Button)view.findViewById(R.id.buttonCancelNewDialog);
        buttonCancel.setOnClickListener(onClickNewDialogHandler);
        Button buttonSave = (Button)view.findViewById(R.id.buttonSaveNewDialog);
        buttonSave.setOnClickListener(onClickNewDialogHandler);
        editNewDialog = (EditText)view.findViewById(R.id.editNewPlaylist);
        textInvalid = (TextView)view.findViewById(R.id.textDialogWrongName);
        textInvalid.setVisibility(View.GONE);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }




    private final View.OnClickListener onClickNewDialogHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String query = editNewDialog.getText().toString();
            if(v.getId() == R.id.buttonSaveNewDialog)
            {
                if(isPlaylistExist(query))
                {
                    textInvalid.setVisibility(View.VISIBLE);
                    textInvalid.setText("playlist already exist");
                }
                else if(query.length() < 4)
                {
                    textInvalid.setVisibility(View.VISIBLE);
                    textInvalid.setText("enter more symbol");
                }
                else if(!Utils.checkSpecialCharacters(query))
                {
                    textInvalid.setVisibility(View.VISIBLE);
                    textInvalid.setText("delete special symbols and blank");
                }
                else
                {
                    createPlatlist(query);
                    textInvalid.setVisibility(View.GONE);
                    dismiss();
                }
            }
            else if (v.getId() == R.id.buttonCancelNewDialog)
            {
                dismiss();
            }
        }
    };
}