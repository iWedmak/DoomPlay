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
import android.widget.EditText;
import android.widget.Toast;
import com.perm.ExceptionHandler.GMailSender;

class ReportDialog extends DialogFragment
{
    private EditText editSubject;
    private EditText editMessage;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialog_report,container,false);
        getDialog().setTitle(getResources().getString(R.string.send_report));
        view.findViewById(R.id.buttonSendReport).setOnClickListener(onClickSendListener);
        view.findViewById(R.id.buttonCancelRepport).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });
        editMessage = (EditText)view.findViewById(R.id.editMessage);
        editSubject = (EditText)view.findViewById(R.id.editSubject);

        return view;
    }
    private final View.OnClickListener onClickSendListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            final String subject = editSubject.getText().toString();
            final String message = editMessage.getText().toString();

            if(subject.equals("") || message.equals(""))
            {
                Toast.makeText(getActivity(), "please fill all fields", Toast.LENGTH_SHORT).show();

            }
            else if(!Utils.isOnline(getActivity()))
            {
                Toast.makeText(getActivity(),"check internet connection",Toast.LENGTH_SHORT).show();
            }
            else
            {
                GMailSender.sendEmail(subject, message);
                Toast.makeText(getActivity(),"message has been successfully sent",Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    };
}