package com.perm.DoomPlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.example.DoomPlay.R;
import com.perm.ExceptionHandler.GMailSender;

class ReportDialog extends SherlockDialogFragment
{
    EditText editSubject;
    EditText editMessage;
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
    View.OnClickListener onClickSendListener = new View.OnClickListener()
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