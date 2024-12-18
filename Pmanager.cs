using System;
using System.IO;
using System.Windows.Forms;
using Gma.System.MouseKeyHook; // Reference to MouseKeyHook library

namespace PasswordClipboard
{
    class App
    {
        private static IKeyboardMouseEvents _globalHook;

        [STAThread]
        static void Main(string[] args)
        {
            _globalHook = Hook.GlobalEvents();
            _globalHook.MouseUpExt += GlobalHook_MouseUpExt;

            Application.Run(new ApplicationContext()); // Keeps the application running
        }

        private static void GlobalHook_MouseUpExt(object sender, MouseEventExtArgs e)
        {
            if (e.Button == MouseButtons.Middle) // Check if the wheel key is pressed
            {
                try
                {
                    // Read the password from the file
                    string filePath = ".\\config.ini";
                    string password = File.ReadAllText(filePath);

                    // Copy the password to the clipboard
                    Clipboard.SetText(password);
            
                    // Paste the password (simulates Ctrl+V)
                    SendKeys.SendWait("^v");

                    Console.WriteLine("Password copied to clipboard and pasted successfully.");
                }
                catch (Exception ex)
                {
                    Console.WriteLine("An error occurred: " + ex.Message);
                }
            }
        }
    }
}
