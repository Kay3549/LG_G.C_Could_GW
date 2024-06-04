#!/bin/bash

SERVER_LOG_PATH="logs/tomcat"
STORAGE_PATH="logs/log_back"
CATALINA_LOG_FILE_NAME="catalina"
LOG_FILE_PATH="logs/log_back/archive_logs_history.log"

LOG_FILES_NM=(
  "producer"
  "consumer"
  "apiClient"
  "apim"
)

ERROR_LOG_FILES_NM=(
  "producer_error"
  "consumer_error"
  "apiClient_error"
  "apim_error"
)

# Maximum days to retain archives
MAX_HISTORY_DAYS=1

# Archive server log files
SERVER_LOG_FILES=$(find "${SERVER_LOG_PATH}" -type f -name "${CATALINA_LOG_FILE_NAME}.????-??-??_*.log") #SERVER_LOG_PATH경로에서 다음과 같은 형태(catalina.2024-05-31_1.log)의 파일들을 찾아 리스트 변수에 넣는다.
for file in ${SERVER_LOG_FILES}; do #리트스 안의 모든 변수들을 하나씩 돌아가면서 'file'이라는 변수로 받아 처리해 준다. 
  base_name=$(basename "$file" .log)# 파일 이름에서 '.log'만 제거하고 'base_name'에 저장
  date_part=$(echo "$base_name" | grep -oE '[0-9]{4}-[0-9]{2}-[0-9]{2}') #날짜만 추출 -> '2024-05-31' 
  new_filename="${CATALINA_LOG_FILE_NAME}.log.${date_part}.tar.gz" # 새로운 포맷의 파일이름 생성
  tar -czvf "${SERVER_LOG_PATH}/${new_filename}" "$file" # file을 해당 경로에 new_filename이름으로 압축 
  tar -czvf "${SERVER_LOG_PATH}/${new_filename}" -C "${SERVER_LOG_PATH}" $(basename -a ${file})
done

archive_grouped_logs() {
  local log_path=$1 #첫번째 파라미터
  local log_file_name=$2 #두번째 파라미터
  echo "log_path: ${log_path}" >> "${LOG_FILE_PATH}" #첫번째 파라미터로 변수가 잘 전달 되었는지 확인해 보기 위해 로그를 남긴다. 남기는 경로는 'LOG_FILE_PATH'
  echo "log_file_name: ${log_file_name}" >> "${LOG_FILE_PATH}"

  declare -A file_groups #associative array 변수 선언. 변수 이름 'file_groups'
  LOG_FILES=$(find "${log_path}" -type f -name "${log_file_name}.????-??-??_*.log") #producer든, consumer든, aplClient든 등등... 'log_file_name'변수로 들어온 이름으로 된 파일들만을 골라서 리스트에 담는다. 

  for file in ${LOG_FILES}; do
    base_name=$(basename "$file" .log)
    echo "base_name: ${base_name}" >> "${LOG_FILE_PATH}"
    date_part=$(echo "$base_name" | grep -oE '[0-9]{4}-[0-9]{2}-[0-9]{2}')
    echo "date_part: ${date_part}" >> "${LOG_FILE_PATH}"
    file_groups["${date_part}"]+="$file " # 날자를 키 값으로 하여 파일들을 배열에 담는다. -> 2차원 배열로 이해하면 쉬울 듯.. ex) 31날짜의 파일들, 28일 날짜의 파일들.. 
  done

  echo "All keys: ${!file_groups[@]}" >> "${LOG_FILE_PATH}" # 배열의 모든 키 값들을 로그로 한번 남겨본다. 

  for date in "${!file_groups[@]}"; do
    echo "Date: ${date}" >> "${LOG_FILE_PATH}" # 배열의 키 값들을 for문 돌면서 하나씩 로그로 남겨본다. 
    files="${file_groups[$date]}" # 키 값(날짜)에 해당하는 모든 파일들을 리스트로 불러와 변수 'files'에 넣는다. 
    echo "Files: ${files}" >> "${LOG_FILE_PATH}"
    new_filename="${log_file_name}.log.${date}.tar.gz" # 새로운 이름으로 tar archive 생성
    # Create the archive without the leading directory path
    tar -czvf "${log_path}/${new_filename}" -C "${log_path}" $(basename -a ${files}) # 경로 포함 없이 전체 경로에서 파일 이름만 추출해서 압축한다. 
  done
}


# Archive grouped application log files
for file in "${LOG_FILES_NM[@]}"; do
  archive_grouped_logs "${STORAGE_PATH}" "$file" # 함수 호출, 매개변수 'STORAGE_PATH' 'file' 전달.
done

for file in ${ERROR_LOG_FILES_NM[@]}; do                               
  archive_grouped_logs "${STORAGE_PATH}" "$file"                  
done

# Clean up old archives older than MAX_HISTORY_DAYS days
find "${SERVER_LOG_PATH}" -type f -name '*.tar.gz' -mtime +${MAX_HISTORY_DAYS} -exec rm -f {} \;
find "${STORAGE_PATH}" -type f -name '*.tar.gz' -mtime +${MAX_HISTORY_DAYS} -exec rm -f {} \;
find "${STORAGE_PATH}" -type f -name '*.log' -mtime +${MAX_HISTORY_DAYS} -exec rm -f {} \;

# Optionally, clean up old log files after archiving
for file in "${LOG_FILES_NM[@]}"; do
  find "${STORAGE_PATH}" -type f -name "${file}.????-??-??_*.log" -exec rm -f {} \;
done

for file in "${ERROR_LOG_FILES_NM[@]}"; do
  find "${STORAGE_PATH}" -type f -name "${file}.????-??-??_*.log" -exec rm -f {} \;
done

find "${SERVER_LOG_PATH}" -type f -name "${CATALINA_LOG_FILE_NAME}.????-??-??_*.log" -exec rm -f {} \;
echo "Archives created and old logs cleaned up."