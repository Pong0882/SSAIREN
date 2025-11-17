"""
응급실 검색 및 GPT 추천 서비스

메디보드 API를 사용하여 주변 응급실을 검색하고
GPT를 통해 환자 상태에 적합한 병원을 추천합니다.
"""

import os
import json
import requests
from typing import Dict, List, Any, Optional
from openai import OpenAI


def get_openai_client():
    """OpenAI 클라이언트 인스턴스 반환"""
    return OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


def convert_structured_data_to_text(structured_data: Dict[str, Any]) -> str:
    """
    구조화된 환자 데이터를 GPT가 이해하기 쉬운 JSON 문자열로 변환

    Args:
        structured_data: LLM 정제 결과 (구조화된 환자 정보)

    Returns:
        str: JSON 형태의 환자 상태 정보
    """
    # JSON을 보기 좋게 포맷팅해서 반환
    # GPT가 JSON을 직접 파싱하고 이해할 수 있음
    return json.dumps(structured_data, ensure_ascii=False, indent=2)


def search_nearby_emergency_rooms(lat: float, lon: float, radius: int) -> Optional[List[Dict[str, Any]]]:
    """
    반경 기반 응급실 검색

    Args:
        lat: 위도
        lon: 경도
        radius: 반경(km)

    Returns:
        List[Dict]: 병원 정보 리스트 (실패 시 None)
    """
    url = "https://mediboard.nemc.or.kr/api/v1/search/handy"
    params = {
        "searchCondition": "radius",
        "lat": lat,
        "lon": lon,
        "radius": radius
    }

    try:
        print(f"\n{'='*70}")
        print(f"[응급실 검색] 반경 {radius}km 내 응급실 검색 중...")
        print(f"[응급실 검색] 위치: 위도 {lat}, 경도 {lon}")
        print(f"{'='*70}\n")

        res = requests.get(url, params=params, timeout=10)
        res.raise_for_status()

        data = res.json()["result"]["data"]

        print(f"[응급실 검색] {len(data)}개 병원 발견\n")

        # 검색된 병원 정보를 로그로 출력 (인코딩 에러 방지)
        try:
            for idx, hospital in enumerate(data, 1):
                print(f"{'-'*70}")
                print(f"[{idx}] {hospital.get('emergencyRoomNickname', 'N/A')}")
                print(f"    거리: {hospital.get('distance', 'N/A')}km")
                print(f"    일반응급: {hospital.get('generalEmergencyAvailable', 'N/A')}/{hospital.get('generalEmergencyTotal', 'N/A')}")
                print()
        except Exception as e:
            print(f"[로그 출력 오류] {e}")

        print(f"{'='*70}\n")

        return data

    except requests.exceptions.RequestException as e:
        print(f"[응급실 검색 오류] API 호출 실패: {e}")
        return None
    except (KeyError, Exception) as e:
        print(f"[응급실 검색 오류] 데이터 파싱 오류: {e}")
        return None


def format_hospital_for_gpt(hospitals: List[Dict[str, Any]]) -> str:
    """
    병원 정보를 GPT가 이해하기 쉬운 형식으로 변환

    Args:
        hospitals: 병원 정보 리스트

    Returns:
        str: 포맷팅된 병원 정보 텍스트
    """
    formatted_text = []

    for idx, hospital in enumerate(hospitals, 1):
        hospital_info = [
            f"\n[병원 {idx}] {hospital.get('emergencyRoomNickname', 'N/A')}"
        ]

        # 기본 정보
        if hospital.get('emergencyRoomName'):
            hospital_info.append(f"정식명: {hospital['emergencyRoomName']}")
        hospital_info.append(f"거리: {hospital.get('distance', 'N/A')}km")
        hospital_info.append(f"주소: {hospital.get('address', 'N/A')}")
        hospital_info.append(f"기관 유형: {hospital.get('emergencyInstitutionType', 'N/A')}")

        # 병상 정보
        hospital_info.append(f"\n병상 현황:")
        hospital_info.append(f"- 일반응급: {hospital.get('generalEmergencyAvailable', 'N/A')}/{hospital.get('generalEmergencyTotal', 'N/A')}")

        if hospital.get('childEmergencyTotal') and int(hospital.get('childEmergencyTotal', 0)) > 0:
            hospital_info.append(f"- 소아응급: {hospital.get('childEmergencyAvailable', 'N/A')}/{hospital.get('childEmergencyTotal', 'N/A')}")

        if hospital.get('deliveryRoomTotal') and hospital.get('deliveryRoomTotal') != '0':
            hospital_info.append(f"- 분만실: {hospital.get('deliveryRoomAvailable', 'N/A')} (총 {hospital.get('deliveryRoomTotal', 0)}개)")

        if hospital.get('npirTotal') and hospital.get('npirTotal') != '0':
            hospital_info.append(f"- 중증외상(NPIR): {hospital.get('npirAvailable', 'N/A')}/{hospital.get('npirTotal', 'N/A')}")

        # 안내 메시지
        if hospital.get('erMessages') and len(hospital.get('erMessages', [])) > 0:
            hospital_info.append(f"\n안내사항:")
            for msg in hospital['erMessages']:
                hospital_info.append(f"  ! {msg.get('message', '')}")

        # 진료 불가 정보
        if hospital.get('unavailableMessages') and len(hospital.get('unavailableMessages', [])) > 0:
            hospital_info.append(f"\n진료 불가:")
            for msg in hospital['unavailableMessages']:
                hospital_info.append(f"  X {msg.get('message', '')}")

        formatted_text.append("\n".join(hospital_info))

    return "\n".join(formatted_text)


def recommend_hospitals_with_gpt(
    patient_condition: str,
    hospitals: List[Dict[str, Any]],
    model: str = "gpt-4.1-2025-04-14"
) -> Dict[str, Any]:
    """
    GPT를 사용하여 병원 추천 및 상세 설명 제공

    Args:
        patient_condition: 환자 상태 정보
        hospitals: 병원 정보 리스트
        model: 사용할 GPT 모델명

    Returns:
        Dict: {
            "recommended_hospitals": List[str],  # 추천 가능한 병원 이름 리스트
            "reasoning": str  # 상위 5개 우선순위 + 제외 병원 이유 설명
        }
    """
    client = get_openai_client()

    # 병원 정보를 GPT가 이해하기 쉬운 형식으로 변환
    hospitals_text = format_hospital_for_gpt(hospitals)

    # 프롬프트 생성
    prompt = f"""당신은 응급 의료 전문가입니다.

**임무**: 환자 상태를 분석하고 추천 가능한 병원들을 선정한 후, 상위 5개의 우선순위와 이유, 그리고 제외된 병원들의 간단한 제외 이유를 설명하세요.

**환자 상태 정보**:
{patient_condition}

**주변 응급실 목록**:
{hospitals_text}

**평가 기준**:
1. 환자 상태에 필요한 시설/장비가 있는지
2. 병상 여부 (가능한 병상이 있는지)
3. 거리 (가까울수록 좋음)
4. 진료 불가 메시지가 있는지 확인
5. 기관 유형 (중증 환자는 상급종합병원/권역응급의료센터 우선)

**출력 형식** (반드시 JSON 형식으로 출력):
{{
  "recommended_hospitals": ["병원1 emergencyRoomNickname", "병원2", "병원3", ...],
  "reasoning": "### 추천 병원 우선순위\\n\\n1순위: 병원명 (거리) - 추천 이유\\n2순위: 병원명 (거리) - 추천 이유\\n...\\n\\n### 제외된 병원\\n\\n- 병원명: 제외 이유\\n- 병원명: 제외 이유\\n..."
}}

**중요 사항**:
- recommended_hospitals: 추천 가능한 **모든 병원 이름**을 배열로 (emergencyRoomNickname 사용)
- reasoning: 마크다운 형식으로 상위 5개 우선순위와 이유를 설명하고, 제외된 병원들의 간단한 이유도 포함
- hospital_name은 위 목록의 "emergencyRoomNickname"과 정확히 일치
- 반드시 순수 JSON만 출력 (코드 블록 금지)"""

    try:
        print(f"\n[GPT 추천] 환자 상태 분석 및 병원 추천 시작...")
        print(f"[GPT 추천] 모델: {model}")
        print(f"[GPT 추천] 환자 상태: {patient_condition[:100]}...")

        response = client.chat.completions.create(
            model=model,
            messages=[
                {
                    "role": "system",
                    "content": "당신은 응급 의료 전문가입니다. 환자 상태를 분석하여 가장 적합한 병원을 추천합니다. 반드시 유효한 JSON만 출력하세요."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            response_format={"type": "json_object"},
            temperature=0.3,
            max_tokens=2000
        )

        content = response.choices[0].message.content.strip()

        # 마크다운 코드 블록 제거 (혹시 모를 경우 대비)
        if content.startswith("```"):
            lines = content.split('\n')
            if lines[0].startswith("```"):
                lines = lines[1:]
            if lines and lines[-1].strip() == "```":
                lines = lines[:-1]
            content = '\n'.join(lines).strip()

        # JSON 파싱
        result = json.loads(content)

        print(f"\n[GPT 추천] 평가 완료:")

        # 추천 병원 출력
        recommended = result.get('recommended_hospitals', [])
        if recommended:
            print(f"[GPT 추천] 추천 가능한 병원: {len(recommended)}개")
            print(f"[GPT 추천] 병원 목록: {', '.join(recommended[:5])}...")

        print(f"[GPT 추천] 설명: {result.get('reasoning', 'N/A')[:200]}...\n")

        return result

    except json.JSONDecodeError as e:
        print(f"[GPT 추천 오류] JSON 파싱 실패: {e}")
        print(f"[GPT 추천 오류] 응답 내용: {content}")
        raise Exception(f"GPT가 유효한 JSON을 반환하지 않았습니다: {e}")
    except Exception as e:
        print(f"[GPT 추천 오류] 오류 발생: {e}")
        raise


def recommend_emergency_hospitals(
    patient_condition: str,
    latitude: float,
    longitude: float,
    radius: int = 10
) -> Dict[str, Any]:
    """
    응급실 검색 및 GPT 추천 통합 함수 (메인 엔트리 포인트)

    Args:
        patient_condition: 환자 상태 정보 (일반 텍스트 또는 JSON 문자열)
        latitude: 위도
        longitude: 경도
        radius: 검색 반경(km)

    Returns:
        Dict: {
            "success": bool,
            "recommended_hospitals": List[str],  # 추천 가능한 병원 이름 리스트
            "total_hospitals_found": int,
            "gpt_reasoning": str,  # 상위 5개 우선순위 + 제외 병원 이유
            "error_message": str (실패 시)
        }
    """
    try:
        print(f"[응급실 추천] 환자 상태 정보 수신")
        print(f"[응급실 추천] 데이터: {patient_condition[:200]}...\n")

        # 1단계: 주변 응급실 검색
        hospitals = search_nearby_emergency_rooms(latitude, longitude, radius)

        if not hospitals or len(hospitals) == 0:
            return {
                "success": False,
                "recommended_hospitals": [],
                "total_hospitals_found": 0,
                "gpt_reasoning": None,
                "error_message": "주변에 응급실을 찾을 수 없습니다."
            }

        # 2단계: GPT를 사용하여 병원 추천
        gpt_result = recommend_hospitals_with_gpt(patient_condition, hospitals)

        # 3단계: 결과 반환
        return {
            "success": True,
            "recommended_hospitals": gpt_result.get("recommended_hospitals", []),
            "total_hospitals_found": len(hospitals),
            "gpt_reasoning": gpt_result.get("reasoning", ""),
            "error_message": None
        }

    except Exception as e:
        print(f"[응급실 추천 오류] {e}")
        return {
            "success": False,
            "recommended_hospitals": [],
            "total_hospitals_found": 0,
            "gpt_reasoning": None,
            "error_message": str(e)
        }
