cd /opt/flashcard-app-installer/bin

expect - "$1" <<- 'EOF'
  set timeout 300 ;# 5 minutes, might timeout on bad internet connection
  spawn {./Flashcard App Installer} [lindex $argv 0]
  expect {
      {Need sudo password:} {
          send "vagrant\r"
          exp_continue
      }
      {Enter your choice: } {
          send "1\r"
          exp_continue
      }
  }
  set result [wait]
  exit [lindex $result 3]
EOF
