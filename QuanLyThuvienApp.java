import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ThuVienException extends Exception {
    public ThuVienException(String message) {
        super(message);
    }
}

class DocGia implements Serializable {
    private String maDocGia;
    private String hoTen;
    private String soDienThoai;

    public DocGia(String maDocGia, String hoTen, String soDienThoai) {
        this.maDocGia = maDocGia;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
    }

    public String getMaDocGia() { return maDocGia; }
    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
}

class Sach implements Serializable {
    private String maSach;
    private String tenSach;
    private String tacGia;
    private int soLuongTon;

    public Sach(String maSach, String tenSach, String tacGia, int soLuongTon) {
        this.maSach = maSach;
        this.tenSach = tenSach;
        this.tacGia = tacGia;
        this.soLuongTon = soLuongTon;
    }

    public String getMaSach() { return maSach; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public String getTacGia() { return tacGia; }
    public void setTacGia(String tacGia) { this.tacGia = tacGia; }
    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }
}

class PhieuMuon implements Serializable {
    private String maPhieu;
    private String tenDocGia;
    private String tenSach;
    private LocalDate ngayMuon;
    private LocalDate hanTra;
    private LocalDate ngayTraThucTe;
    private String trangThai;

    public PhieuMuon(String maPhieu, String tenDocGia, String tenSach, LocalDate ngayMuon) {
        this.maPhieu = maPhieu;
        this.tenDocGia = tenDocGia;
        this.tenSach = tenSach;
        this.ngayMuon = ngayMuon;
        this.hanTra = ngayMuon.plusDays(14);
        this.ngayTraThucTe = null;
        this.trangThai = "Đang Mượn";
    }

    public String getMaPhieu() { return maPhieu; }
    public String getTenDocGia() { return tenDocGia; }
    public String getTenSach() { return tenSach; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    
    public LocalDate getNgayMuon() { return ngayMuon; }
    public LocalDate getHanTra() { return hanTra; }
    public LocalDate getNgayTraThucTe() { return ngayTraThucTe; }
    public void setNgayTraThucTe(LocalDate ngayTraThucTe) { this.ngayTraThucTe = ngayTraThucTe; }

    public String getNgayMuonFormatted() { return ngayMuon.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    public String getHanTraFormatted() { return hanTra.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    public String getNgayTraThucTeFormatted() {
        return (ngayTraThucTe != null) ? ngayTraThucTe.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa trả";
    }

    public long tinhTienPhat(LocalDate ngayTra) {
        if (ngayTra.isAfter(hanTra)) {
            long soNgayTre = ChronoUnit.DAYS.between(hanTra, ngayTra);
            return soNgayTre * 2000;
        }
        return 0;
    }
}

public class QuanLyThuvienApp extends JFrame {
    private List<Sach> danhSachSach = new ArrayList<>();
    private List<DocGia> danhSachDocGia = new ArrayList<>();
    private List<PhieuMuon> danhSachPhieuMuon = new ArrayList<>();

    private DefaultTableModel modelSach;
    private JTable tableSach;
    private JTextField txtMaSach, txtTenSach, txtTacGia, txtSoLuong, txtTimKiemSach;

    private DefaultTableModel modelDocGia;
    private JTable tableDocGia;
    private JTextField txtMaDG, txtTenDG, txtSdtDG;

    private DefaultTableModel modelPhieu;
    private JTable tablePhieu;
    private JLabel lblMaPhieuAuto;
    private JComboBox<String> cbDocGiaMuon, cbSachMuon;
    
    // Nhãn thanh trạng thái ở chân cửa sổ
    private JLabel lblStatus;

    private final String FILE_SACH = "sach.dat";
    private final String FILE_DOCGIA = "docgia.dat";
    private final String FILE_PHIEU = "phieumuon.dat";

    public QuanLyThuvienApp() {
        setTitle("Hệ Thống Quản Lý Thư Viện Chuyên Nghiệp");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        taiDuLieuTuFile();

        // Layout chính của cửa sổ
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        tabbedPane.addTab("Quản Lý Sách", taoTabQuanLySach());
        tabbedPane.addTab("Quản Lý Độc Giả", taoTabQuanLyDocGia());
        tabbedPane.addTab("Quản Lý Mượn / Trả", taoTabMuonTra());

        add(tabbedPane, BorderLayout.CENTER);

        // Tạo Thanh Trạng Thái (Status Bar) dưới cùng
        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        panelStatus.setBorder(BorderFactory.createEtchedBorder());
        lblStatus = new JLabel();
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelStatus.add(lblStatus);
        add(panelStatus, BorderLayout.SOUTH);

        capNhatTrangThai();
    }

    private JPanel taoTabQuanLySach() {
        JPanel panelMain = new JPanel(new BorderLayout(10, 10));
        panelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));

        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBorder(BorderFactory.createTitledBorder(" Thông Tin Sách "));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1; panelInput.add(new JLabel("Mã Sách:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; txtMaSach = new JTextField(); panelInput.add(txtMaSach, gbc);

        gbc.gridx = 2; gbc.weightx = 0.1; panelInput.add(new JLabel("Tên Sách:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4; txtTenSach = new JTextField(); panelInput.add(txtTenSach, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelInput.add(new JLabel("Tác Giả:"), gbc);
        gbc.gridx = 1; txtTacGia = new JTextField(); panelInput.add(txtTacGia, gbc);

        gbc.gridx = 2; panelInput.add(new JLabel("Số Lượng Tồn:"), gbc);
        gbc.gridx = 3; txtSoLuong = new JTextField(); panelInput.add(txtSoLuong, gbc);

        panelTop.add(panelInput);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnThem = new JButton("Thêm Sách");
        JButton btnSua = new JButton("Sửa Sách");
        JButton btnXoa = new JButton("Xóa Sách");
        JButton btnLamMoi = new JButton("Làm Mới");
        panelButtons.add(btnThem); panelButtons.add(btnSua); panelButtons.add(btnXoa); panelButtons.add(btnLamMoi);
        panelTop.add(panelButtons);

        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelSearch.setBorder(BorderFactory.createTitledBorder(" Tìm Kiếm "));
        panelSearch.add(new JLabel("Nhập tên sách:"));
        txtTimKiemSach = new JTextField(25);
        panelSearch.add(txtTimKiemSach);
        JButton btnTim = new JButton("Tìm Kiếm");
        JButton btnTatCa = new JButton("Tất Cả");
        panelSearch.add(btnTim); panelSearch.add(btnTatCa);
        panelTop.add(panelSearch);

        panelMain.add(panelTop, BorderLayout.NORTH);

        String[] cols = {"Mã Sách", "Tên Sách", "Tác Giả", "Số Lượng Tồn"};
        modelSach = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableSach = new JTable(modelSach);
        tableSach.setRowHeight(25);

        tableSach.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasF, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasF, row, col);
                if (!isSel) {
                    try {
                        int sl = Integer.parseInt(t.getValueAt(row, 3).toString());
                        if (sl <= 3) {
                            c.setBackground(new Color(255, 230, 230));
                            c.setForeground(new Color(180, 0, 0));
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE); c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(t.getSelectionBackground());
                    c.setForeground(t.getSelectionForeground());
                }
                if (col == 3) setHorizontalAlignment(JLabel.RIGHT);
                else if (col == 0) setHorizontalAlignment(JLabel.CENTER);
                else setHorizontalAlignment(JLabel.LEFT);
                return c;
            }
        });

        panelMain.add(new JScrollPane(tableSach), BorderLayout.CENTER);

        tableSach.getSelectionModel().addListSelectionListener(e -> {
            int r = tableSach.getSelectedRow();
            if (r >= 0) {
                txtMaSach.setText(modelSach.getValueAt(r, 0).toString());
                txtMaSach.setEnabled(false);
                txtTenSach.setText(modelSach.getValueAt(r, 1).toString());
                txtTacGia.setText(modelSach.getValueAt(r, 2).toString());
                txtSoLuong.setText(modelSach.getValueAt(r, 3).toString());
            }
        });

        btnThem.addActionListener(e -> themSach());
        btnSua.addActionListener(e -> suaSach());
        btnXoa.addActionListener(e -> xoaSach());
        btnLamMoi.addActionListener(e -> lamMoiFormSach());
        btnTim.addActionListener(e -> {
            String kw = txtTimKiemSach.getText().trim().toLowerCase();
            List<Sach> res = danhSachSach.stream().filter(s -> s.getTenSach().toLowerCase().contains(kw)).collect(Collectors.toList());
            capNhatBangSach(res);
        });
        btnTatCa.addActionListener(e -> { txtTimKiemSach.setText(""); capNhatBangSach(danhSachSach); });

        return panelMain;
    }

    private JPanel taoTabQuanLyDocGia() {
        JPanel panelMain = new JPanel(new BorderLayout(10, 10));
        panelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));

        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBorder(BorderFactory.createTitledBorder(" Thông Tin Độc Giả "));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1; panelInput.add(new JLabel("Mã Độc Giả:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; txtMaDG = new JTextField(); panelInput.add(txtMaDG, gbc);

        gbc.gridx = 2; gbc.weightx = 0.1; panelInput.add(new JLabel("Họ Tên:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4; txtTenDG = new JTextField(); panelInput.add(txtTenDG, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelInput.add(new JLabel("Số Điện Thoại:"), gbc);
        gbc.gridx = 1; txtSdtDG = new JTextField(); panelInput.add(txtSdtDG, gbc);

        panelTop.add(panelInput);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnThemDG = new JButton("Thêm Độc Giả");
        JButton btnSuaDG = new JButton("Sửa Độc Giả");
        JButton btnXoaDG = new JButton("Xóa Độc Giả");
        JButton btnLamMoiDG = new JButton("Làm Mới");
        panelButtons.add(btnThemDG); panelButtons.add(btnSuaDG); panelButtons.add(btnXoaDG); panelButtons.add(btnLamMoiDG);
        panelTop.add(panelButtons);
        panelMain.add(panelTop, BorderLayout.NORTH);

        String[] cols = {"Mã Độc Giả", "Họ Tên", "Số Điện Thoại"};
        modelDocGia = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDocGia = new JTable(modelDocGia);
        tableDocGia.setRowHeight(25);
        panelMain.add(new JScrollPane(tableDocGia), BorderLayout.CENTER);

        tableDocGia.getSelectionModel().addListSelectionListener(e -> {
            int r = tableDocGia.getSelectedRow();
            if (r >= 0) {
                txtMaDG.setText(modelDocGia.getValueAt(r, 0).toString());
                txtMaDG.setEnabled(false);
                txtTenDG.setText(modelDocGia.getValueAt(r, 1).toString());
                txtSdtDG.setText(modelDocGia.getValueAt(r, 2).toString());
            }
        });

        btnThemDG.addActionListener(e -> themDocGia());
        btnSuaDG.addActionListener(e -> suaDocGia());
        btnXoaDG.addActionListener(e -> xoaDocGia());
        btnLamMoiDG.addActionListener(e -> lamMoiFormDocGia());

        return panelMain;
    }

    private JPanel taoTabMuonTra() {
        JPanel panelMain = new JPanel(new BorderLayout(10, 10));
        panelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelInput = new JPanel(new GridLayout(3, 2, 8, 8));
        panelInput.setBorder(BorderFactory.createTitledBorder(" Lập Phiếu Mượn Sách "));

        panelInput.add(new JLabel("Mã Phiếu (Tự động):"));
        lblMaPhieuAuto = new JLabel();
        lblMaPhieuAuto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelInput.add(lblMaPhieuAuto);

        panelInput.add(new JLabel("Chọn Độc Giả:"));
        cbDocGiaMuon = new JComboBox<>();
        panelInput.add(cbDocGiaMuon);

        panelInput.add(new JLabel("Chọn Sách Mượn:"));
        cbSachMuon = new JComboBox<>();
        panelInput.add(cbSachMuon);

        JPanel panelTop = new JPanel(new BorderLayout(5, 5));
        panelTop.add(panelInput, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnTaoPhieu = new JButton("Tạo Phiếu Mượn");
        JButton btnTraSach = new JButton("Xác Nhận Trả Sách");
        panelButtons.add(btnTaoPhieu); panelButtons.add(btnTraSach);
        panelTop.add(panelButtons, BorderLayout.SOUTH);

        panelMain.add(panelTop, BorderLayout.NORTH);

        String[] cols = {"Mã Phiếu", "Độc Giả", "Sách Mượn", "Ngày Mượn", "Hạn Trả", "Ngày Trả Thực Tế", "Trạng Thái"};
        modelPhieu = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablePhieu = new JTable(modelPhieu);
        tablePhieu.setRowHeight(25);

        tablePhieu.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (!isSelected) {
                    String trangThai = (value != null) ? value.toString() : "";
                    if (trangThai.equals("Đang Mượn")) {
                        c.setForeground(new Color(210, 105, 30));
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        c.setBackground(Color.WHITE);
                    } else if (trangThai.equals("Đã Trả")) {
                        c.setForeground(new Color(0, 128, 0));
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setForeground(Color.BLACK);
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });

        panelMain.add(new JScrollPane(tablePhieu), BorderLayout.CENTER);

        btnTaoPhieu.addActionListener(e -> taoPhieuMuon());
        btnTraSach.addActionListener(e -> traSach());

        return panelMain;
    }

    private void capNhatTrangThai() {
        if (lblStatus != null) {
            long dangMuonCount = danhSachPhieuMuon.stream().filter(p -> p.getTrangThai().equals("Đang Mượn")).count();
            lblStatus.setText("  Trạng thái hệ thống: [ Sẵn sàng ]   |   Tổng số sách: " + danhSachSach.size() + 
                              "   |   Tổng độc giả: " + danhSachDocGia.size() + 
                              "   |   Phiếu đang mượn: " + dangMuonCount);
        }
    }

    private void themSach() {
        try {
            String ma = txtMaSach.getText().trim();
            String ten = txtTenSach.getText().trim();
            String tg = txtTacGia.getText().trim();
            String slStr = txtSoLuong.getText().trim();

            if (ma.isEmpty() || ten.isEmpty() || tg.isEmpty() || slStr.isEmpty()) {
                throw new ThuVienException("Vui lòng điền đầy đủ thông tin sách!");
            }
            if (danhSachSach.stream().anyMatch(s -> s.getMaSach().equalsIgnoreCase(ma))) {
                throw new ThuVienException("Mã sách này đã tồn tại trong kho!");
            }

            int sl = Integer.parseInt(slStr);
            if (sl < 0) throw new ThuVienException("Số lượng tồn phải lớn hơn hoặc bằng 0!");

            danhSachSach.add(new Sach(ma, ten, tg, sl));
            luuDuLieuVaoFile();
            capNhatBangSach(danhSachSach);
            capNhatComboBoxes();
            capNhatTrangThai();
            JOptionPane.showMessageDialog(this, "Thêm sách thành công!");
            lamMoiFormSach();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng tồn phải là kiểu số nguyên hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (ThuVienException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cảnh báo nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void suaSach() {
        try {
            String ma = txtMaSach.getText().trim();
            String ten = txtTenSach.getText().trim();
            String tg = txtTacGia.getText().trim();
            String slStr = txtSoLuong.getText().trim();

            if (ma.isEmpty()) throw new ThuVienException("Vui lòng chọn sách cần sửa từ bảng!");
            int sl = Integer.parseInt(slStr);
            if (sl < 0) throw new ThuVienException("Số lượng không được âm!");

            Sach s = danhSachSach.stream().filter(item -> item.getMaSach().equalsIgnoreCase(ma)).findFirst().orElse(null);
            if (s != null) {
                s.setTenSach(ten);
                s.setTacGia(tg);
                s.setSoLuongTon(sl);
                luuDuLieuVaoFile();
                capNhatBangSach(danhSachSach);
                capNhatComboBoxes();
                capNhatTrangThai();
                JOptionPane.showMessageDialog(this, "Cập nhật sách thành công!");
                lamMoiFormSach();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaSach() {
        try {
            String ma = txtMaSach.getText().trim();
            if (ma.isEmpty()) throw new ThuVienException("Vui lòng chọn sách cần xóa trên bảng!");

            Sach s = danhSachSach.stream().filter(item -> item.getMaSach().equalsIgnoreCase(ma)).findFirst().orElse(null);
            if (s != null) {
                boolean dangDuocMuon = danhSachPhieuMuon.stream()
                        .anyMatch(p -> p.getTenSach().equalsIgnoreCase(s.getTenSach()) && p.getTrangThai().equals("Đang Mượn"));
                if (dangDuocMuon) {
                    throw new ThuVienException("Không thể xóa sách này vì đang có độc giả mượn chưa trả!");
                }

                if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa sách mã: " + ma + "?", "Xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    danhSachSach.remove(s);
                    luuDuLieuVaoFile();
                    capNhatBangSach(danhSachSach);
                    capNhatComboBoxes();
                    capNhatTrangThai();
                    lamMoiFormSach();
                }
            }
        } catch (ThuVienException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void themDocGia() {
        try {
            String ma = txtMaDG.getText().trim();
            String ten = txtTenDG.getText().trim();
            String sdt = txtSdtDG.getText().trim();

            if (ma.isEmpty() || ten.isEmpty() || sdt.isEmpty()) {
                throw new ThuVienException("Vui lòng điền đầy đủ thông tin độc giả!");
            }
            if (danhSachDocGia.stream().anyMatch(d -> d.getMaDocGia().equalsIgnoreCase(ma))) {
                throw new ThuVienException("Mã độc giả đã tồn tại!");
            }

            danhSachDocGia.add(new DocGia(ma, ten, sdt));
            luuDuLieuVaoFile();
            capNhatBangDocGia();
            capNhatComboBoxes();
            capNhatTrangThai();
            JOptionPane.showMessageDialog(this, "Thêm độc giả thành công!");
            lamMoiFormDocGia();
        } catch (ThuVienException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void suaDocGia() {
        try {
            String ma = txtMaDG.getText().trim();
            String ten = txtTenDG.getText().trim();
            String sdt = txtSdtDG.getText().trim();

            if (ma.isEmpty()) throw new ThuVienException("Vui lòng chọn độc giả cần sửa từ bảng!");
            DocGia d = danhSachDocGia.stream().filter(item -> item.getMaDocGia().equalsIgnoreCase(ma)).findFirst().orElse(null);
            if (d != null) {
                d.setHoTen(ten);
                d.setSoDienThoai(sdt);
                luuDuLieuVaoFile();
                capNhatBangDocGia();
                capNhatComboBoxes();
                capNhatTrangThai();
                JOptionPane.showMessageDialog(this, "Cập nhật độc giả thành công!");
                lamMoiFormDocGia();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaDocGia() {
        String ma = txtMaDG.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn độc giả cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa độc giả mã: " + ma + "?", "Xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            danhSachDocGia.removeIf(d -> d.getMaDocGia().equalsIgnoreCase(ma));
            luuDuLieuVaoFile();
            capNhatBangDocGia();
            capNhatComboBoxes();
            capNhatTrangThai();
            lamMoiFormDocGia();
        }
    }

    private void taoPhieuMuon() {
        try {
            String maP = sinhMaPhieuTuDong();
            String tenDG = (String) cbDocGiaMuon.getSelectedItem();
            String tenS = (String) cbSachMuon.getSelectedItem();

            if (tenDG == null || tenS == null) {
                throw new ThuVienException("Chưa có đủ thông tin Độc Giả hoặc Sách để lập phiếu!");
            }

            Sach sachMuon = danhSachSach.stream().filter(s -> s.getTenSach().equalsIgnoreCase(tenS)).findFirst().orElse(null);
            if (sachMuon == null || sachMuon.getSoLuongTon() <= 0) {
                throw new ThuVienException("Sách này đã hết số lượng tồn trong kho, không thể mượn!");
            }

            sachMuon.setSoLuongTon(sachMuon.getSoLuongTon() - 1);

            PhieuMuon pMoi = new PhieuMuon(maP, tenDG, tenS, LocalDate.now());
            danhSachPhieuMuon.add(pMoi);

            luuDuLieuVaoFile();
            capNhatBangPhieu();
            capNhatBangSach(danhSachSach);
            capNhatTrangThai();
            taoMaPhieuMoi();

            JOptionPane.showMessageDialog(this, "Lập phiếu mượn thành công!\nHạn trả: " + pMoi.getHanTraFormatted());
        } catch (ThuVienException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cảnh báo nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void traSach() {
        try {
            int row = tablePhieu.getSelectedRow();
            if (row < 0) {
                throw new ThuVienException("Vui lòng chọn một dòng phiếu mượn trên bảng để thực hiện trả sách!");
            }

            String maP = modelPhieu.getValueAt(row, 0).toString();
            PhieuMuon p = danhSachPhieuMuon.stream().filter(item -> item.getMaPhieu().equalsIgnoreCase(maP)).findFirst().orElse(null);

            if (p != null) {
                if (p.getTrangThai().equals("Đã Trả")) {
                    throw new ThuVienException("Phiếu mượn này đã được trả trước đó rồi, không thể trả lại lần hai!");
                }

                LocalDate ngayTraThucTe = LocalDate.now();
                p.setNgayTraThucTe(ngayTraThucTe);
                p.setTrangThai("Đã Trả");

                Sach sachTra = danhSachSach.stream().filter(s -> s.getTenSach().equalsIgnoreCase(p.getTenSach())).findFirst().orElse(null);
                if (sachTra != null) {
                    sachTra.setSoLuongTon(sachTra.getSoLuongTon() + 1);
                }

                long tienPhat = p.tinhTienPhat(ngayTraThucTe);

                luuDuLieuVaoFile();
                capNhatBangPhieu();
                capNhatBangSach(danhSachSach);
                capNhatTrangThai();

                String thongBao = "Xác nhận trả sách thành công!";
                if (tienPhat > 0) {
                    thongBao += "\n⚠️ Độc giả trả trễ hạn và bị phạt: " + String.format("%,d", tienPhat) + " VNĐ";
                } else {
                    thongBao += "\n✨ Độc giả trả đúng hạn. Không có tiền phạt!";
                }
                JOptionPane.showMessageDialog(this, thongBao, "Kết Quả Trả Sách", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ThuVienException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String sinhMaPhieuTuDong() {
        return "P" + String.format("%02d", danhSachPhieuMuon.size() + 1);
    }

    private void taoMaPhieuMoi() {
        if (lblMaPhieuAuto != null) {
            lblMaPhieuAuto.setText(sinhMaPhieuTuDong());
        }
    }

    private void luuDuLieuVaoFile() {
        try (ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream(FILE_SACH));
             ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(FILE_DOCGIA));
             ObjectOutputStream oos3 = new ObjectOutputStream(new FileOutputStream(FILE_PHIEU))) {
            oos1.writeObject(danhSachSach);
            oos2.writeObject(danhSachDocGia);
            oos3.writeObject(danhSachPhieuMuon);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void taiDuLieuTuFile() {
        try {
            File f1 = new File(FILE_SACH);
            File f2 = new File(FILE_DOCGIA);
            File f3 = new File(FILE_PHIEU);

            if (f1.exists() && f2.exists() && f3.exists()) {
                try (ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream(FILE_SACH));
                     ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(FILE_DOCGIA));
                     ObjectInputStream ois3 = new ObjectInputStream(new FileInputStream(FILE_PHIEU))) {
                    danhSachSach = (List<Sach>) ois1.readObject();
                    danhSachDocGia = (List<DocGia>) ois2.readObject();
                    danhSachPhieuMuon = (List<PhieuMuon>) ois3.readObject();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        taiDuLieuMau();
        luuDuLieuVaoFile();
    }

    private void taiDuLieuMau() {
        danhSachSach.add(new Sach("S01", "Cho tôi xin một vé đi tuổi thơ", "Nguyễn Nhật Ánh", 10));
        danhSachSach.add(new Sach("S02", "Chí Phèo", "Nam Cao", 2));
        danhSachSach.add(new Sach("S03", "Mắt Biếc", "Nguyễn Nhật Ánh", 5));

        danhSachDocGia.add(new DocGia("DG01", "Nguyễn Văn An", "0901234567"));
        danhSachDocGia.add(new DocGia("DG02", "Trần Thị Bình", "0912345678"));

        danhSachPhieuMuon.add(new PhieuMuon("P01", "Nguyễn Văn An", "Mắt Biếc", LocalDate.now().minusDays(18)));
    }

    private void capNhatBangSach(List<Sach> list) {
        modelSach.setRowCount(0);
        for (Sach s : list) {
            modelSach.addRow(new Object[]{s.getMaSach(), s.getTenSach(), s.getTacGia(), s.getSoLuongTon()});
        }
    }

    private void capNhatBangDocGia() {
        modelDocGia.setRowCount(0);
        for (DocGia d : danhSachDocGia) {
            modelDocGia.addRow(new Object[]{d.getMaDocGia(), d.getHoTen(), d.getSoDienThoai()});
        }
    }

    private void capNhatBangPhieu() {
        modelPhieu.setRowCount(0);
        for (PhieuMuon p : danhSachPhieuMuon) {
            modelPhieu.addRow(new Object[]{
                p.getMaPhieu(), p.getTenDocGia(), p.getTenSach(), 
                p.getNgayMuonFormatted(), p.getHanTraFormatted(), 
                p.getNgayTraThucTeFormatted(), p.getTrangThai()
            });
        }
    }

    private void capNhatComboBoxes() {
        if (cbDocGiaMuon != null) {
            cbDocGiaMuon.removeAllItems();
            for (DocGia d : danhSachDocGia) cbDocGiaMuon.addItem(d.getHoTen());
        }
        if (cbSachMuon != null) {
            cbSachMuon.removeAllItems();
            for (Sach s : danhSachSach) cbSachMuon.addItem(s.getTenSach());
        }
    }

    private void lamMoiFormSach() {
        txtMaSach.setText(""); txtMaSach.setEnabled(true);
        txtTenSach.setText(""); txtTacGia.setText(""); txtSoLuong.setText("");
        tableSach.clearSelection();
    }

    private void lamMoiFormDocGia() {
        txtMaDG.setText(""); txtMaDG.setEnabled(true);
        txtTenDG.setText(""); txtSdtDG.setText("");
        tableDocGia.clearSelection();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            QuanLyThuvienApp app = new QuanLyThuvienApp();
            app.capNhatBangSach(app.danhSachSach);
            app.capNhatBangDocGia();
            app.capNhatBangPhieu();
            app.capNhatComboBoxes();
            app.taoMaPhieuMoi();
            app.setVisible(true);
        });
    }
}