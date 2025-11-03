//DispatchDetail.tsx
import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Modal,
} from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../App';

type Props = NativeStackScreenProps<RootStackParamList, 'DispatchDetail'>;

function DispatchDetail({ navigation, route }: Props) {
  const { dispatchId, patientName, location, date } = route.params;

  return (
    <Modal
      animationType="fade"
      transparent={true}
      visible={true}
      onRequestClose={() => navigation.goBack()}
    >
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          {/* 헤더 */}
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>출동지령 상세 정보</Text>
            <TouchableOpacity
              onPress={() => navigation.goBack()}
              style={styles.closeButton}
            >
              <Text style={styles.closeText}>✕</Text>
            </TouchableOpacity>
          </View>

          {/* 내용 */}
          <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={false}>
            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>지령번호</Text>
              <Text style={styles.infoValue}>{dispatchId}</Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>구급출동지</Text>
              <Text style={styles.infoValue}>{patientName}</Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>위치 / 고급도</Text>
              <Text style={styles.infoValue}>{location}</Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>신고 일시</Text>
              <Text style={styles.infoValue}>{date}</Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>신고자 정보</Text>
              <Text style={styles.infoValue}>
                김철수(010-0000-0000)
              </Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>수령시각</Text>
              <Text style={styles.infoValue}>2023-11-15 05:19:35</Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>출동요청 불라인은 쌔생 상황 중지현장 출동위맥도스</Text>
              <Text style={styles.infoValue}>
                참고 위치정보
              </Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.infoSection}>
              <Text style={styles.infoLabel}>내용정보</Text>
              <Text style={styles.infoValue}>
                9 둘 강남대 병원 열림
              </Text>
            </View>
          </ScrollView>

          {/* 확인 버튼 */}
          <TouchableOpacity 
            style={styles.confirmButton}
            onPress={() => navigation.goBack()}
          >
            <Text style={styles.confirmButtonText}>내 병원 출동</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalContent: {
    backgroundColor: '#2a2a2a',
    borderRadius: 12,
    borderWidth: 2,
    borderColor: '#ff4444',
    width: '100%',
    maxWidth: 400,
    maxHeight: '80%',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#3a3a3a',
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
  },
  closeButton: {
    padding: 4,
  },
  closeText: {
    fontSize: 20,
    color: '#999999',
  },
  scrollContent: {
    padding: 16,
  },
  infoSection: {
    paddingVertical: 12,
  },
  infoLabel: {
    fontSize: 12,
    color: '#999999',
    marginBottom: 6,
  },
  infoValue: {
    fontSize: 14,
    color: '#ffffff',
    lineHeight: 20,
  },
  divider: {
    height: 1,
    backgroundColor: '#3a3a3a',
  },
  confirmButton: {
    backgroundColor: '#3b7cff',
    margin: 16,
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
  },
  confirmButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#ffffff',
  },
});

export default DispatchDetail;