package fpt.capstone.iContact.service;
import fpt.capstone.iContact.dto.Convert;
import fpt.capstone.iContact.dto.request.ContactExportDTO;
import fpt.capstone.iContact.model.AddressInformation;
import fpt.capstone.iContact.model.Contact;
import fpt.capstone.iContact.model.Salution;
import fpt.capstone.iContact.repository.AddressInformationRepository;
import fpt.capstone.iContact.repository.SalutionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class ExcelUploadService {
    private Convert converter;
    private AddressInformationRepository addressInformationRepository;
    private SalutionRepository salutionRepository;
    private static final String[] HEADER = {
            "Id","Fist Name","Last Name","Middle Name","Report to","lead Salution Name",
            "Title", "Email", "Phone","Department", "Mobile",
            "Fax", "Street",  "City", "Province", "PostalCode", "Country"
    };
    private static final String SHEET_NAME = "Contact";

    public ByteArrayInputStream dataToExecel(List<Contact> list) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < HEADER.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADER[i]);
            }

            List<ContactExportDTO> contactExportDTOS = converter.ContactEntityListToContactExportDTOList(list);
            int rowIndex = 1;

            for (ContactExportDTO contactExportDTO : contactExportDTOS) {
                Row dataRow = sheet.createRow(rowIndex++);

                AddressInformation addressInformation = contactExportDTO.getAddressInformationId() != null ?
                        addressInformationRepository.findById(contactExportDTO.getAddressInformationId()).orElse(null) : null;
                String leadSalutionName = contactExportDTO.getContactSalutionId() != null ?
                        salutionRepository.findById(contactExportDTO.getContactSalutionId())
                                .map(Salution::getLeadSalutionName).orElse("") : "";


                dataRow.createCell(0).setCellValue(contactExportDTO.getContactId());
                dataRow.createCell(1).setCellValue(Optional.ofNullable(contactExportDTO.getFirstName()).orElse(""));
                dataRow.createCell(2).setCellValue(Optional.ofNullable(contactExportDTO.getLastName()).orElse(""));
                dataRow.createCell(3).setCellValue(Optional.ofNullable(contactExportDTO.getMiddleName()).orElse(""));
                dataRow.createCell(4).setCellValue(Optional.ofNullable(contactExportDTO.getReport_to()).orElse(0L));
                dataRow.createCell(5).setCellValue(Optional.ofNullable(leadSalutionName).orElse(""));
                dataRow.createCell(6).setCellValue(Optional.ofNullable(contactExportDTO.getTitle()).orElse(""));
                dataRow.createCell(7).setCellValue(Optional.ofNullable(contactExportDTO.getEmail()).orElse(""));
                dataRow.createCell(8).setCellValue(Optional.ofNullable(contactExportDTO.getPhone()).orElse(""));
                dataRow.createCell(9).setCellValue(Optional.ofNullable(contactExportDTO.getDepartment()).orElse(""));
                dataRow.createCell(10).setCellValue(Optional.ofNullable(contactExportDTO.getMobile()).orElse(""));
                dataRow.createCell(11).setCellValue(Optional.ofNullable(contactExportDTO.getFax()).orElse(""));
                dataRow.createCell(12).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getStreet).orElse(""));
                dataRow.createCell(13).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getCity).orElse(""));
                dataRow.createCell(14).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getProvince).orElse(""));
                dataRow.createCell(15).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getPostalCode).orElse(""));
                dataRow.createCell(16).setCellValue(Optional.ofNullable(addressInformation).map(AddressInformation::getCountry).orElse(""));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("fail to export File");
            return null;
        } finally {
            workbook.close();
            out.close();
        }
    }
}
