# 🔰 Programmers Badge Generator

[프로그래머스](https://programmers.co.kr/) 문제 풀이 현황을 보여주는 **GitHub 배지 프로젝트**<br>
프로그래머스에서 푼 문제 실적을 GitHub 프로필에 배지 형태로 시각화해줍니다. 배지는 **매주 자동으로 갱신**되며, 원버튼 수동 갱신도 지원합니다.<br><br>

## ♻️ 디자인 리빌딩 및 절차 간소화

[tomy8964/Programmers_Badge_Generator](https://github.com/tomy8964/Programmers_Badge_Generator?tab=readme-ov-file)를 기반으로 디자인을 새롭게 구성하고, 배지 생성 절차를 간소화하였습니다.

|                        이전 배지 디자인                         |                       현재 배지 디자인                        |
| :-------------------------------------------------------------: | :-----------------------------------------------------------: |
| ![Previous badge design](./readme-assets/prev_badge_design.svg) | ![Current badge design](./readme-assets/cur_badge_design.svg) |

## 🌟 레벨 별 배지 디자인

|                          Lv1. Design                          |                          Lv2. Design                          |                          Lv3. Design                          |
| :-----------------------------------------------------------: | :-----------------------------------------------------------: | :-----------------------------------------------------------: |
| ![Lv1. badge design](./readme-assets/level1_badge_design.svg) | ![Lv2. badge design](./readme-assets/level2_badge_design.svg) | ![Lv3. badge design](./readme-assets/level3_badge_design.svg) |

|                          Lv4. Design                          |                          Lv5. Design                          |                          Challenger<br>Design                           |
| :-----------------------------------------------------------: | :-----------------------------------------------------------: | :---------------------------------------------------------------------: |
| ![Lv4. badge design](./readme-assets/level4_badge_design.svg) | ![Lv5. badge design](./readme-assets/level5_badge_design.svg) | ![Challenger badge design](./readme-assets/challenger_badge_design.svg) |

> 💡 일부 숨겨진 도전과제를 달성하면 통계치가 금색으로 하이라이트됩니다.

<br>

## ⚙️ 프로젝트 설정 (Project Setup)

1. 이 레포지토리를 포크합니다.  
   → **Fork this repository.**

2. 포크한 레포지토리의 "Actions" 탭으로 이동해 "Enable" 버튼을 클릭합니다.  
   → **Open the "Actions" tab of your fork and click the "Enable" button.**

3. 환경 변수를 등록합니다.  
   → **Set up environment variables.**

   - 레포지토리 Settings > Secrets and variables > Actions 로 이동하여 아래 값을 등록합니다.  
     → **Go to Settings > Secrets and variables > Actions and add the following variables:**

     - `GH_PAT`: GitHub Personal Access Token  
       → **GitHub Personal Access Token**
     - `PROGRAMMERS_TOKEN_ID`: 프로그래머스 계정 이메일  
       → **Your Programmers account email**
     - `PROGRAMMERS_TOKEN_PW`: 프로그래머스 계정 비밀번호  
       → **Your Programmers account password**

4. 배지가 정상적으로 생성되었는지 확인합니다.  
   → **Check if the badge is generated correctly.**

   - 포크한 레포지토리의 `result` 폴더에 `result.svg` 파일이 생성되어 있는지 확인하고,  
      자신의 프로그래머스 정보와 일치하는지 확인합니다.  
     → **Go to the `result` folder in your fork and verify that `result.svg` matches your data.**

5. GitHub 프로필에 프로그래머스 배지를 등록합니다.  
   → **Add the Programmers badge to your GitHub profile.**

   - GitHub 프로필에 해당하는 레포지토리의 `README.md` 파일에 다음 코드를 삽입하세요:  
      → **Edit the `README.md` of your GitHub profile repo and insert the following:**

     ```
     ![Programmers Badge](https://raw.githubusercontent.com/{your-github-id}/programmers-badge-generator/main/output/result.svg)
     ```

     <br>

## 🔄 배지 수동 갱신 (Manually Refreshing the Badge)

배지를 수동으로 갱신하고 싶다면,  
포크한 레포지토리의 **Actions** 탭으로 이동하여  
가장 최근에 실행된 워크플로우를 선택한 후 우측 상단의 **Re-run jobs** 버튼을 클릭하세요.

→ **To manually refresh the badge**,  
go to the **Actions** tab of your forked repository,  
select the most recent workflow run, and click the **Re-run jobs** button in the top right corner.

> ⚠️ 프로그래머스 통계에 변화가 없을 경우, 워크플로우가 실패로 표시되며 배지는 갱신되지 않습니다.  
> → **Note:** If there are no changes in your Programmers stats, the workflow will fail and the badge will not be updated.
