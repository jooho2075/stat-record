# 기록 항목 명세 (Record Spec)

## 1. 기록 단위
- 모든 기록은 **타석(Plate Appearance)** 단위로 입력한다.
- 타자 기록과 투수 기록은 같은 타석 데이터에서 함께 계산한다.
- 같은 사실을 두 번 입력하지 않도록, 투수 기록은 가능한 한 타석 데이터에서 계산한다.

## 2. 타석 결과 코드
| 코드 | 항목 | 타수 포함 | 안타 | 사용 지표 |
|---|---|---|---|---|
| 1B | 1루타 | O | O | AVG, OBP, SLG, wOBA, BABIP |
| 2B | 2루타 | O | O | AVG, OBP, SLG, wOBA, BABIP |
| 3B | 3루타 | O | O | AVG, OBP, SLG, wOBA, BABIP |
| HR | 홈런 | O | O | AVG, OBP, SLG, wOBA, FIP |
| BB | 볼넷 | X | X | OBP, wOBA, FIP |
| IBB | 고의사구 | X | X | OBP |
| HBP | 사구 | X | X | OBP, wOBA, FIP |
| SO | 삼진 | O | X | BABIP, K/9, FIP |
| SH | 희생번트 | X | X | - |
| SF | 희생플라이 | X | X | OBP, BABIP |
| GO | 땅볼 아웃 | O | X | AVG, BABIP |
| FO | 뜬공 아웃 | O | X | AVG, BABIP |
| LO | 라인드라이브 아웃 | O | X | AVG, BABIP |
| ROE | 실책 출루 | O | X | AVG |
| FC | 야수선택 | O | X | AVG |

## 3. 타석 부가 정보
| 항목 | 설명 | 필수 |
|---|---|---|
| 이닝 / 초·말 | 타석이 발생한 이닝 | O |
| 아웃카운트 | 타석 시작 시점 아웃 수 | O |
| 타자 / 투수 | 해당 타석의 타자와 투수 | O |
| 타점 | 해당 타석으로 발생한 타점 | O |
| 득점 주자 | 득점한 선수 | O |
| 도루 / 도루실패 | 별도 이벤트로 기록 | 선택 |

## 4. 투수 기록
| 항목 | 계산 방식 |
|---|---|
| 투구 이닝 | 아웃 수로 저장 (예: 5⅓이닝 = 16아웃) |
| 피안타, 볼넷, 삼진, 피홈런 | 타석 데이터에서 자동 계산 |
| 실점 | 타석 데이터에서 계산 |
| 자책점 | 별도 입력 (실책 관련 판정 필요) |

## 5. 제공 지표

### 5-1. 타격 지표 (Batting Metrics)
- **OPS (On-base Plus Slugging):** 출루율과 장타율을 더한 값으로, 타자의 전반적인 생산력을 간단히 평가합니다.
- **wOBA (Weighted On-Base Average):** 출루에 성공하는 방식(볼넷, 안타, 홈런 등)별로 가중치를 다르게 부여해 선수의 진짜 타격 능력을 측정합니다.
- **간이 wRC+ (Simplified Weighted Runs Created Plus):** 리그 평균(100 기준) 대비 타자의 득점 생산력을 나타냅니다.
  - 원래 wRC+는 구장 특성(파크 팩터)을 보정하지만, 동호회 구장별 데이터로는 파크 팩터를 산출하기 어려워 **구장 보정 없이 리그 평균과만 비교**합니다.

### 5-2. 투구 지표 (Pitching Metrics)
- **FIP (Fielding Independent Pitching):** 수비의 도움이나 운을 배제하고, 투수가 온전히 통제할 수 있는 삼진, 볼넷, 사구, 홈런만을 가지고 계산한 평균자책점(ERA) 형태의 지표입니다.
- **WHIP (Walks plus Hits per Inning Pitched):** 이닝당 출루 허용률로, 피안타와 볼넷을 합해 이닝 수로 나눕니다.

### 5-3. 종합 지표 (Overall Metrics)
- **간이 WAR (Simplified Wins Above Replacement):** 대체 선수에 비해 승리에 얼마나 더 기여했는지를 나타냅니다.
  - 원래 WAR은 타격, 주루, 수비, 포지션 보정을 모두 포함하지만, 본 서비스는 수비 데이터를 수집하지 않으므로 **타격(또는 투구) 기여도만으로 계산**합니다. 따라서 공식 WAR과 직접 비교할 수 없습니다.

> 참고: 동호회 경기는 표본이 작아 지표 변동이 큽니다. 최소 타석/이닝 기준에 미달하는 선수는 "표본 부족"으로 표시합니다.

## 6. 지표 산출식

### 클래식
- AVG = H / AB
- OBP = (H + BB + HBP) / (AB + BB + HBP + SF)
- SLG = TB / AB
- OPS = OBP + SLG
- ERA = ER × 정규이닝 / IP
- WHIP = (BB + H) / IP

### 세이버메트릭스
- ISO = SLG - AVG
- BABIP = (H - HR) / (AB - SO - HR + SF)
- wOBA = (wBB×uBB + wHBP×HBP + w1B×1B + w2B×2B + w3B×3B + wHR×HR) / (AB + BB - IBB + SF + HBP)
- FIP = (13×HR + 3×(BB + HBP) - 2×SO) / IP + FIP 상수
- K/9, BB/9
- 간이 wRC+, 간이 WAR: 산출식 확정 필요 (7. 미결정 사항 참고)

## 7. 미결정 사항
- [ ] 정규 이닝 수 (7이닝 / 9이닝 / 리그별 설정)
- [ ] wOBA 가중치, FIP 상수 출처 (KBO 기준값 차용 여부)
- [ ] 간이 wRC+ 산출식 및 리그 평균 기준
- [ ] 간이 WAR의 대체 선수 기준 (예: 리그 하위 몇 %)
- [ ] 자책점 판정을 기록원이 직접 입력할지
- [ ] 최소 타석/이닝 기준 (표본 부족 표시용)
- [ ] 병살, 낫아웃 등 특수 상황 처리